package maxparser.trainer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.exception.TrainingException;
import maxparser.io.DependencyReader;
import maxparser.io.DependencyWriter;
import maxparser.io.ObjectReader;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.manager.Manager;
import maxparser.trainer.lbfgs.LBFGS.ExceptionWithIflag;
import maxparser.trainer.lbfgs.SimpleLBFGS;

public class RMLETrainer extends Trainer {
    @Override
    public void train(Manager manager, Decoder decoder, ParserModel model, String trainfile, String devfile,
            String logfile, String modelfile, int numTrainInst, int numDevInst) throws TrainingException,
                    ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        System.out.println("Num Sentences: " + numTrainInst);
        System.out.println("Num Threads:   " + model.threadNum());
        System.out.println("Cost:          " + model.cost());
        System.out.println("Tau:           " + model.tau());
        
        final double eta = 1e-6;
        final int maxIter = model.maxIter();

        int threadNum = model.threadNum();
        Manager[] managers = createManagers(manager, trainfile, threadNum, numTrainInst, model);

        double cost = model.cost();
        double tau = model.tau();

        String trainforest = model.trainforest();
        int nsize = model.featureSize();

        double bestAcc = 0.0;
        double bestC = 0.0;
        double bestTau = 0.0;
        PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logfile)));

        for (double c = ((devfile != null && cost > 0.020) ? 0.020 : cost); c <= cost; c += 0.005) {
            for (double t = ((devfile != null && tau > 0.5) ? 0.5 : tau); t <= tau + 0.05; t += 0.1) {
                double old_obj = 1e+37;
                int converge = 0;
                SimpleLBFGS lbfgs = new SimpleLBFGS(nsize);
                System.out.println(String.format("current cost:  %.3f, tau:  %.2f", c, t));
                for (int itr = 0; itr < maxIter; ++itr) {
                    System.out.flush();
                    long clock = System.currentTimeMillis() / 1000;

                    RewardGradientCollectThread[] threads = new RewardGradientCollectThread[threadNum];
                    int unit = (numTrainInst % threadNum == 0) ? numTrainInst / threadNum : numTrainInst / threadNum + 1;
                    for (int i = 0; i < threadNum; ++i) {
                        int start = unit * i;
                        int end = start + unit;
                        if (end > numTrainInst) {
                            end = numTrainInst;
                        }
                        String[] tokens = trainforest.split("\\.");
                        String forestfile = tokens[0] + i + "." + tokens[1];
                        threads[i] = new RewardGradientCollectThread(start, end, managers[i], decoder, forestfile, model, t);
                    }

                    for (int i = 0; i < threadNum; ++i) {
                        threads[i].start();
                    }
                    for (int i = 0; i < threadNum; ++i) {
                        try {
                            threads[i].join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.exit(0);
                        }
                    }

                    for (int i = 1; i < threadNum; ++i) {
                        threads[0].obj += threads[i].obj;
                    }

                    for (int i = 1; i < threadNum; ++i) {
                        for (int j = 0; j < nsize; ++j) {
                            threads[0].gradient[j] += threads[i].gradient[j];
                        }
                    }

                    threads[0].obj *= c;

                    for (int j = 0; j < nsize; ++j) {
                        threads[0].obj += model.paramAt(j) * model.paramAt(j) / 2.0;// (2.0
                                                                                // *
                                                                                // c);
                        threads[0].gradient[j] *= c;
                        threads[0].gradient[j] += model.paramAt(j);// / c;
                    }

                    double diff = itr == 0 ? 1.0 : Math.abs(old_obj - threads[0].obj) / old_obj;
                    System.out.println("iter=" + itr + " obj=" + String.format("%.8f", threads[0].obj) + " diff="
                            + String.format("%.8f", diff) + " time=" + (System.currentTimeMillis() / 1000 - clock) + "s.");
                    old_obj = threads[0].obj;

                    if (diff < eta) {
                        converge++;
                    } else {
                        converge = 0;
                    }

                    if (itr > maxIter || converge == 3) {
                        break;
                    }

                    try {
                        if (model.update(lbfgs, threads[0].obj, threads[0].gradient) == 0) {
                            break;
                        }
                    } catch (ExceptionWithIflag e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                }

                System.out.print("Saving Model...");
                long clock = System.currentTimeMillis() / 1000;
                saveModel(model, modelfile);
                System.out.println("Done. Took: " + (System.currentTimeMillis() / 1000 - clock) + "s.");

                if (devfile != null) {
                    // calc current accuracy
                    logWriter.println(String.format("Cost:  %.3f", c));
                    double acc = evalCurrentAcc(manager, decoder, model, devfile, logWriter, numDevInst);
                    logWriter.println("------------------------------------------");
                    logWriter.flush();
                    if (bestAcc < acc) {
                        bestAcc = acc;
                        saveModel(model, modelfile);
                        bestC = c;
                        bestTau = t;
                    }
                }
            }
        }
        logWriter.close();
        System.out.println("Final Dev Accuracy: " + bestAcc + "% (Cost: " + bestC + " Tau: " + bestTau + ")");
    }
    
    protected double evalCurrentAcc(Manager manager, Decoder decoder, ParserModel tempModel, String devfile,
            PrintWriter logWriter, int numDevInst)
                    throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        ObjectReader in = new ObjectReader(tempModel.devforest());
        String tempfile = "tmp/result.tmp";

        DependencyWriter tempWriter = DependencyWriter.createDependencyWriter(tempModel.getWriter());
        tempWriter.startWriting(tempfile);

        for (int j = 0; j < numDevInst; ++j) {
            DependencyInstance inst = null;
            inst = manager.readInstance(in, tempModel);

            Pair<FeatureVector, String>[] d = decoder.decode(manager, inst, 1, tempModel);
            String[] res = d[0].second.split(" ");
            int[] heads = new int[inst.length()];
            String[] types = new String[inst.length()];
            heads[0] = -1;
            types[0] = "<no-type>";
            for (int k = 1; k < inst.length(); k++) {
                String[] trip = res[k - 1].split("[\\|:]");
                heads[k] = Integer.parseInt(trip[0]);
                types[k] = tempModel.getType(Integer.parseInt(trip[2]));
            }

            tempWriter.write(inst, heads, types);
        }

        // close in & tempWriter
        in.close();
        tempWriter.close();

        // evaluate current acc
        return maxparser.Evaluator.evaluate(devfile, tempfile, logWriter, tempModel);
    }

    private Manager[] createManagers(Manager manager, String trainfile, int threadNum, int numTrainInst,
            ParserModel model)
                    throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        DependencyReader reader = DependencyReader.createDependencyReader(model.getReader());
        reader.startReading(trainfile);
        int unit = (numTrainInst % threadNum == 0) ? numTrainInst / threadNum : numTrainInst / threadNum + 1;
        int i = 0;
        int[] maxLength = new int[threadNum];
        int wh = 0;
        for (int j = 0; j < numTrainInst; ++j) {
            if (j % unit == 0 && j != 0) {
                ++i;
            }
            DependencyInstance instance = reader.getNext(model);
            if (instance.length() > maxLength[i]) {
                maxLength[i] = instance.length();
                if (maxLength[i] > maxLength[wh]) {
                    wh = i;
                }
            }
        }
        reader.close();
        Manager[] managers = new Manager[threadNum];
        for (int j = 0; j < threadNum; ++j) {
            managers[j] = j == wh ? manager : manager.clone(maxLength[j], model.typeSize());
        }
        return managers;
    }

    class RewardGradientCollectThread extends Thread {
        private int start;
        private int end;
        private double tau;
        private Manager manager;
        private Decoder decoder;
        private String forestfile;
        private ParserModel model;

        public double obj;
        public double[] gradient = null;

        public RewardGradientCollectThread(int start, int end, Manager manager, Decoder decoder, String forestfile,
                ParserModel model, double tau) {
            this.start = start;
            this.end = end;
            this.manager = manager;
            this.decoder = decoder;
            this.forestfile = forestfile;
            this.model = model;
            this.tau = tau;
            gradient = new double[model.featureSize()];
        }

        public void run() {
            try {
                obj = 0.0;
                ObjectReader in1 = new ObjectReader(forestfile);
                ObjectReader in2 = new ObjectReader(forestfile);
                for (int i = start; i < end; ++i) {
                    obj += decoder.calcRewardLogLinearGradient(gradient, manager, model, tau, in1, in2);
                }
                in1.close();
                in2.close();
            } catch (TrainingException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
