package maxparser.parser.decoder;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.Util;
import maxparser.exception.TrainingException;
import maxparser.io.ObjectReader;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.forest.SingleEdgeForest;
import maxparser.parser.decoder.forest.ForestItem;
import maxparser.parser.manager.Manager;
import maxparser.parser.marginal.Marginal;
import maxparser.parser.decoder.forest.indextuple.BasicForestIndexTuple;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;
import maxparser.parser.marginal.ioforest.InOutForest;
import maxparser.parser.marginal.ioforest.SingleEdgeInOutForest;

public class SingleEdgeProjDecoder extends Decoder {

    @Override
    public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
        int length = inst.length();
        short zero = (short) 0;
        short one = (short) 1;
        short minusOne = (short) -1;

        manager.getTypes(length, model);

        SingleEdgeForest forest = new SingleEdgeForest(length - 1, K);
        BasicForestIndexTuple forestIndex = new BasicForestIndexTuple();

        for (short s = 0; s < length; ++s) {
            forestIndex.setIndex(s, s, zero, one);
            forest.addItem(forestIndex, minusOne, minusOne, 0.0, null, null);

            forestIndex.setIndex(s, s, one, one);
            forest.addItem(forestIndex, minusOne, minusOne, 0.0, null, null);
        }

        SingleEdgeIndexTuple index0 = new SingleEdgeIndexTuple();
        SingleEdgeIndexTuple index1 = new SingleEdgeIndexTuple();

        for (short j = 1; j < length; ++j) {
            for (short s = 0; s + j < length; ++s) {
                short t = (short) (s + j);
                // positive index
                index0.par = s;
                index0.ch = t;
                manager.getType(inst, index0, model);
                // negative index
                index1.par = t;
                index1.ch = s;
                manager.getType(inst, index1, model);

                for (short r = s; r < t; ++r) {
                    ForestItem[] b1 = forest.getItems(forestIndex.setIndex(s, r, zero, one));
                    ForestItem[] c1 = forest.getItems(forestIndex.setIndex((short) (r + 1), t, one, one));
                    int[][] pairs = forest.getKBestPairs(b1, c1);
                    for (int k = 0; k < pairs.length; ++k) {
                        if (pairs[k][0] == -1 || pairs[k][1] == -1) {
                            break;
                        }

                        int comp1 = pairs[k][0];
                        int comp2 = pairs[k][1];

                        double score = b1[comp1].score + c1[comp2].score + manager.getScore(index0);
                        boolean added1 = forest.addItem(forestIndex.setIndex(s, t, zero, zero), r, (short) index0.type,
                                score, b1[comp1], c1[comp2]);

                        score = b1[comp1].score + c1[comp2].score + manager.getScore(index1);
                        boolean added2 = forest.addItem(forestIndex.setIndex(s, t, one, zero), r, (short) index1.type,
                                score, b1[comp1], c1[comp2]);

                        if (!added1 && !added2) {
                            break;
                        }
                    }
                }
                for (short r = s; r <= t; ++r) {
                    if (r != s) {
                        ForestItem[] b1 = forest.getItems(forestIndex.setIndex(s, r, zero, zero));
                        ForestItem[] c1 = forest.getItems(forestIndex.setIndex(r, t, zero, one));
                        int[][] pairs = forest.getKBestPairs(b1, c1);
                        for (int k = 0; k < pairs.length; ++k) {
                            if (pairs[k][0] == -1 || pairs[k][1] == -1) {
                                break;
                            }

                            int comp1 = pairs[k][0];
                            int comp2 = pairs[k][1];

                            double score = b1[comp1].score + c1[comp2].score;
                            if (!forest.addItem(forestIndex.setIndex(s, t, zero, one), r, minusOne, score, b1[comp1],
                                    c1[comp2])) {
                                break;
                            }
                        }
                    }
                    if (r != t) {
                        ForestItem[] b1 = forest.getItems(forestIndex.setIndex(s, r, one, one));
                        ForestItem[] c1 = forest.getItems(forestIndex.setIndex(r, t, one, zero));
                        int[][] pairs = forest.getKBestPairs(b1, c1);
                        for (int k = 0; k < pairs.length; ++k) {
                            if (pairs[k][0] == -1 || pairs[k][1] == -1) {
                                break;
                            }

                            int comp1 = pairs[k][0];
                            int comp2 = pairs[k][1];

                            double score = b1[comp1].score + c1[comp2].score;
                            if (!forest.addItem(forestIndex.setIndex(s, t, one, one), r, minusOne, score, b1[comp1],
                                    c1[comp2])) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return forest.getBestParses(inst, manager, model);
    }

    @Override
    public double calcLogLinearGradient(double[] gradient, Manager manager, ParserModel model, ObjectReader in1,
            ObjectReader in2) throws TrainingException, IOException, ClassNotFoundException {
        DependencyInstance inst = manager.readInstance(in1, model);
        SingleEdgeInOutForest ioForest = new SingleEdgeInOutForest(inst.length());
        double z = inside(inst.length(), ioForest, manager);
        double obj = z - model.getScore(inst.getFeatureVector());
        // calc outside alpha
        outside(inst.length(), ioForest, manager);
        // calc gradient
        getGradient(gradient, ioForest, z, inst.length(), manager, model, in2);
        return obj;
    }

    protected void getGradient(double[] gradient, InOutForest ioForest, double z, int length, Manager manager,
            ParserModel model, ObjectReader in) throws ClassNotFoundException, IOException {
        // read feature vector of current instance
        int[] keys = (int[]) in.readObject();
        int last = in.readInt();
        if (last != -4) {
            throw new IOException("last number is not equal to -4");
        }
        updateGradient(gradient, keys, -1.0);
        // read current instance itself
        in.readObject();
        last = in.readInt();
        if (last != -1) {
            throw new IOException("last number is not equal to -1");
        }

        BasicForestIndexTuple index = new BasicForestIndexTuple();
        for (short par = 0; par < length; ++par) {
            for (short ch = 0; ch < length; ++ch) {
                if (ch == par) {
                    continue;
                }

                keys = (int[]) in.readObject();
                short min = par < ch ? par : ch;
                short max = par < ch ? ch : par;
                short ph = (short) (par < ch ? 0 : 1);
                index.setIndex(min, max, ph, (short) 0);
                double m = Math.exp(ioForest.getBeta(index) + ioForest.getAlpha(index) - z);
                updateGradient(gradient, keys, m);
            }
        }
        last = in.readInt();
        if (last != -3) {
            throw new IOException("last number is not equal to -3");
        }
    }

    @Override
    public double calcRewardLogLinearGradient(double[] gradient, Manager manager, ParserModel model, double tau, ObjectReader in1,
            ObjectReader in2) throws TrainingException, IOException, ClassNotFoundException {
        DependencyInstance inst = manager.readInstance(in1, model);
        SingleEdgeInOutForest ioForest = new SingleEdgeInOutForest(inst.length());
        double z = inside(inst.length(), ioForest, manager);
        // calc outside alpha
        outside(inst.length(), ioForest, manager);

        // calc rewards
        double[][] rewards = new double[inst.length()][inst.length()];
        calcRewards(rewards, inst, tau);
        SingleEdgeInOutForest rewardIOForest = new SingleEdgeInOutForest(inst.length());
        double z_reward = inside_reward(inst.length(), rewardIOForest, rewards);
        // calc reward outsize alpha
        outside_reward(inst.length(), rewardIOForest, rewards);
        // calc gradient
        double obj_reward = getRewardGradient(gradient, ioForest, rewardIOForest, z, z_reward, inst.length(), manager,
                model, in2);
        // obj = z - obj_reward;
        return z - obj_reward;
    }

    protected double getRewardGradient(double[] gradient, InOutForest ioForest, InOutForest rewardIOForest, double z,
            double z_reward, int length, Manager manager, ParserModel model, ObjectReader in)
                    throws ClassNotFoundException, IOException {
        // read feature vector of current instance
        int[] keys = (int[]) in.readObject();
        int last = in.readInt();
        if (last != -4) {
            throw new IOException("last number is not equal to -4");
        }
        // read current instance itself
        in.readObject();
        last = in.readInt();
        if (last != -1) {
            throw new IOException("last number is not equal to -1");
        }

        double obj_reward = 0.0;
        SingleEdgeIndexTuple tid = new SingleEdgeIndexTuple();
        BasicForestIndexTuple fid = new BasicForestIndexTuple();
        for (short par = 0; par < length; ++par) {
            for (short ch = 0; ch < length; ++ch) {
                if (ch == par) {
                    continue;
                }

                // calc m
                keys = (int[]) in.readObject();
                short min = par < ch ? par : ch;
                short max = par < ch ? ch : par;
                short ph = (short) (par < ch ? 0 : 1);
                fid.setIndex(min, max, ph, (short) 0);
                double m = Math.exp(ioForest.getBeta(fid) + ioForest.getAlpha(fid) - z);
                // calc m_reward
                double m_reward = Math.exp(rewardIOForest.getBeta(fid) + rewardIOForest.getAlpha(fid) - z_reward);
                // calc reward objective
                tid.par = par;
                tid.ch = ch;
                obj_reward += manager.getScore(tid) * m_reward;
                // update gradient
                updateGradient(gradient, keys, m - m_reward);
            }
        }
        last = in.readInt();
        if (last != -3) {
            throw new IOException("last number is not equal to -3");
        }

        return obj_reward;
    }

    protected void calcRewards(double[][] rewards, DependencyInstance inst, double tau) {
        int length = inst.length();
        for (int i = 1; i < length; ++i) {
            rewards[inst.heads[i]][i] = 1.0 / tau;
        }
    }

    protected double inside(int length, InOutForest ioForest, Manager manager) {
        short zero = (short) 0;
        short one = (short) 1;
        BasicForestIndexTuple fid00 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid01 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid10 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid11 = new BasicForestIndexTuple();

        BasicForestIndexTuple fid = new BasicForestIndexTuple();

        SingleEdgeIndexTuple index0 = new SingleEdgeIndexTuple();
        SingleEdgeIndexTuple index1 = new SingleEdgeIndexTuple();

        for (short j = 1; j < length; ++j) {
            for (short s = 0; s + j < length; ++s) {
                short t = (short) (s + j);
                // positive index
                index0.par = s;
                index0.ch = t;

                // negative index
                index1.par = t;
                index1.ch = s;

                // init beta
                // incomplete spans
                ioForest.addBeta(fid00.setIndex(s, t, zero, zero), Double.NEGATIVE_INFINITY);
                ioForest.addBeta(fid10.setIndex(s, t, one, zero), Double.NEGATIVE_INFINITY);

                // complete spans
                ioForest.addBeta(fid01.setIndex(s, t, zero, one), Double.NEGATIVE_INFINITY);
                ioForest.addBeta(fid11.setIndex(s, t, one, one), Double.NEGATIVE_INFINITY);

                for (short r = s; r < t; ++r) {
                    double val = Util.logsumexp(ioForest.getBeta(fid00), ioForest.getBeta(fid.setIndex(s, r, zero, one))
                            + ioForest.getBeta(fid.setIndex((short) (r + 1), t, one, one)) + manager.getScore(index0));
                    ioForest.addBeta(fid00, val);

                    val = Util.logsumexp(ioForest.getBeta(fid10), ioForest.getBeta(fid.setIndex(s, r, zero, one))
                            + ioForest.getBeta(fid.setIndex((short) (r + 1), t, one, one)) + manager.getScore(index1));
                    ioForest.addBeta(fid10, val);
                }

                for (short r = s; r <= t; ++r) {
                    if (r != s) {
                        double val = Util.logsumexp(ioForest.getBeta(fid01),
                                ioForest.getBeta(fid.setIndex(s, r, zero, zero))
                                        + ioForest.getBeta(fid.setIndex(r, t, zero, one)));
                        ioForest.addBeta(fid01, val);
                    }

                    if (r != t) {
                        double val = Util.logsumexp(ioForest.getBeta(fid11),
                                ioForest.getBeta(fid.setIndex(s, r, one, one))
                                        + ioForest.getBeta(fid.setIndex(r, t, one, zero)));
                        ioForest.addBeta(fid11, val);
                    }
                }
            }
        }
        return Util.logsumexp(ioForest.getBeta(fid.setIndex(zero, (short) (length - 1), zero, one)),
                ioForest.getBeta(fid.setIndex(zero, (short) (length - 1), one, one)));
    }

    protected double inside_reward(int length, InOutForest ioForest, double[][] rewards) {
        short zero = (short) 0;
        short one = (short) 1;
        BasicForestIndexTuple fid00 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid01 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid10 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid11 = new BasicForestIndexTuple();

        BasicForestIndexTuple fid = new BasicForestIndexTuple();

        for (short j = 1; j < length; ++j) {
            for (short s = 0; s + j < length; ++s) {
                short t = (short) (s + j);
                // init beta
                // incomplete spans
                ioForest.addBeta(fid00.setIndex(s, t, zero, zero), Double.NEGATIVE_INFINITY);
                ioForest.addBeta(fid10.setIndex(s, t, one, zero), Double.NEGATIVE_INFINITY);

                // complete spans
                ioForest.addBeta(fid01.setIndex(s, t, zero, one), Double.NEGATIVE_INFINITY);
                ioForest.addBeta(fid11.setIndex(s, t, one, one), Double.NEGATIVE_INFINITY);

                for (short r = s; r < t; ++r) {
                    double val = Util.logsumexp(ioForest.getBeta(fid00), ioForest.getBeta(fid.setIndex(s, r, zero, one))
                            + ioForest.getBeta(fid.setIndex((short) (r + 1), t, one, one)) + rewards[s][t]);
                    ioForest.addBeta(fid00, val);

                    val = Util.logsumexp(ioForest.getBeta(fid10), ioForest.getBeta(fid.setIndex(s, r, zero, one))
                            + ioForest.getBeta(fid.setIndex((short) (r + 1), t, one, one)) + rewards[t][s]);
                    ioForest.addBeta(fid10, val);
                }

                for (short r = s; r <= t; ++r) {
                    if (r != s) {
                        double val = Util.logsumexp(ioForest.getBeta(fid01),
                                ioForest.getBeta(fid.setIndex(s, r, zero, zero))
                                        + ioForest.getBeta(fid.setIndex(r, t, zero, one)));
                        ioForest.addBeta(fid01, val);
                    }

                    if (r != t) {
                        double val = Util.logsumexp(ioForest.getBeta(fid11),
                                ioForest.getBeta(fid.setIndex(s, r, one, one))
                                        + ioForest.getBeta(fid.setIndex(r, t, one, zero)));
                        ioForest.addBeta(fid11, val);
                    }
                }
            }
        }
        return Util.logsumexp(ioForest.getBeta(fid.setIndex(zero, (short) (length - 1), zero, one)),
                ioForest.getBeta(fid.setIndex(zero, (short) (length - 1), one, one)));
    }

    protected void outside(int length, InOutForest ioForest, Manager manager) {
        short zero = (short) 0;
        short one = (short) 1;
        BasicForestIndexTuple fid00 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid01 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid10 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid11 = new BasicForestIndexTuple();

        BasicForestIndexTuple fidA = new BasicForestIndexTuple();
        BasicForestIndexTuple fidB = new BasicForestIndexTuple();

        SingleEdgeIndexTuple index0 = new SingleEdgeIndexTuple();
        SingleEdgeIndexTuple index1 = new SingleEdgeIndexTuple();

        short end = (short) (length - 2);
        for (short j = end; j >= 1; --j) {
            for (short s = 0; s + j < length; ++s) {
                short t = (short) (s + j);
                // init alpha
                // incomplete spans
                ioForest.addAlpha(fid00.setIndex(s, t, zero, zero), Double.NEGATIVE_INFINITY);
                ioForest.addAlpha(fid10.setIndex(s, t, one, zero), Double.NEGATIVE_INFINITY);

                // complete spans
                ioForest.addAlpha(fid01.setIndex(s, t, zero, one), Double.NEGATIVE_INFINITY);
                ioForest.addAlpha(fid11.setIndex(s, t, one, one), Double.NEGATIVE_INFINITY);

                for (short r = 0; r < s; ++r) {
                    // positive index
                    index0.par = r;
                    index0.ch = t;

                    // negative index
                    index1.par = t;
                    index1.ch = r;

                    // alpha[s][t][0][1]
                    double val = Util.logsumexp(ioForest.getAlpha(fid01),
                            ioForest.getBeta(fidB.setIndex(r, s, zero, zero))
                                    + ioForest.getAlpha(fidA.setIndex(r, t, zero, one)));
                    ioForest.addAlpha(fid01, val);

                    // alpha[s][t][1][1]
                    fidA.comp = zero;
                    val = Util.logsumexp(ioForest.getAlpha(fid11),
                            ioForest.getBeta(fidB.setIndex(r, (short) (s - 1), zero, one)) + ioForest.getAlpha(fidA)
                                    + manager.getScore(index0));
                    ioForest.addAlpha(fid11, val);

                    fidA.dir = one;
                    fidA.comp = zero;
                    val = Util.logsumexp(ioForest.getAlpha(fid11),
                            ioForest.getBeta(fidB) + ioForest.getAlpha(fidA) + manager.getScore(index1));
                    ioForest.addAlpha(fid11, val);
                }

                for (short r = (short) (t + 1); r < length; ++r) {
                    index0.par = s;
                    index0.ch = r;

                    // negative index
                    index1.par = r;
                    index1.ch = s;

                    // alpha[s][t][0][1]
                    double val = Util.logsumexp(ioForest.getAlpha(fid01),
                            ioForest.getBeta(fidB.setIndex((short) (t + 1), r, one, one))
                                    + ioForest.getAlpha(fidA.setIndex(s, r, zero, zero)) + manager.getScore(index0));
                    ioForest.addAlpha(fid01, val);

                    fidA.dir = one;
                    val = Util.logsumexp(ioForest.getAlpha(fid01),
                            ioForest.getBeta(fidB) + ioForest.getAlpha(fidA) + manager.getScore(index1));
                    ioForest.addAlpha(fid01, val);

                    fidA.comp = one;
                    // alpha[s][t][1][1]
                    val = Util.logsumexp(ioForest.getAlpha(fid11),
                            ioForest.getBeta(fidB.setIndex(t, r, one, zero)) + ioForest.getAlpha(fidA));
                    ioForest.addAlpha(fid11, val);
                }

                // alpha[s][t][0][0]
                for (short r = t; r < length; ++r) {
                    double val = Util.logsumexp(ioForest.getAlpha(fid00),
                            ioForest.getBeta(fidB.setIndex(t, r, zero, one))
                                    + ioForest.getAlpha(fidA.setIndex(s, r, zero, one)));
                    ioForest.addAlpha(fid00, val);
                }
                // alpha[s][t][1][0]
                for (short r = 0; r <= s; ++r) {
                    double val = Util.logsumexp(ioForest.getAlpha(fid10),
                            ioForest.getBeta(fidB.setIndex(r, s, one, one))
                                    + ioForest.getAlpha(fidA.setIndex(r, t, one, one)));
                    ioForest.addAlpha(fid10, val);
                }
            }
        }
    }

    protected void outside_reward(int length, InOutForest ioForest, double[][] rewards) {
        short zero = (short) 0;
        short one = (short) 1;
        BasicForestIndexTuple fid00 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid01 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid10 = new BasicForestIndexTuple();
        BasicForestIndexTuple fid11 = new BasicForestIndexTuple();

        BasicForestIndexTuple fidA = new BasicForestIndexTuple();
        BasicForestIndexTuple fidB = new BasicForestIndexTuple();

        short end = (short) (length - 2);
        for (short j = end; j >= 1; --j) {
            for (short s = 0; s + j < length; ++s) {
                short t = (short) (s + j);
                // init alpha
                // incomplete spans
                ioForest.addAlpha(fid00.setIndex(s, t, zero, zero), Double.NEGATIVE_INFINITY);
                ioForest.addAlpha(fid10.setIndex(s, t, one, zero), Double.NEGATIVE_INFINITY);

                // complete spans
                ioForest.addAlpha(fid01.setIndex(s, t, zero, one), Double.NEGATIVE_INFINITY);
                ioForest.addAlpha(fid11.setIndex(s, t, one, one), Double.NEGATIVE_INFINITY);

                for (short r = 0; r < s; ++r) {
                    // alpha[s][t][0][1]
                    double val = Util.logsumexp(ioForest.getAlpha(fid01),
                            ioForest.getBeta(fidB.setIndex(r, s, zero, zero))
                                    + ioForest.getAlpha(fidA.setIndex(r, t, zero, one)));
                    ioForest.addAlpha(fid01, val);

                    // alpha[s][t][1][1]
                    fidA.comp = zero;
                    val = Util.logsumexp(ioForest.getAlpha(fid11),
                            ioForest.getBeta(fidB.setIndex(r, (short) (s - 1), zero, one)) + ioForest.getAlpha(fidA)
                                    + rewards[r][t]);
                    ioForest.addAlpha(fid11, val);

                    fidA.dir = one;
                    fidA.comp = zero;
                    val = Util.logsumexp(ioForest.getAlpha(fid11),
                            ioForest.getBeta(fidB) + ioForest.getAlpha(fidA) + rewards[t][r]);
                    ioForest.addAlpha(fid11, val);
                }

                for (short r = (short) (t + 1); r < length; ++r) {
                    // alpha[s][t][0][1]
                    double val = Util.logsumexp(ioForest.getAlpha(fid01),
                            ioForest.getBeta(fidB.setIndex((short) (t + 1), r, one, one))
                                    + ioForest.getAlpha(fidA.setIndex(s, r, zero, zero)) + rewards[s][r]);
                    ioForest.addAlpha(fid01, val);

                    fidA.dir = one;
                    val = Util.logsumexp(ioForest.getAlpha(fid01),
                            ioForest.getBeta(fidB) + ioForest.getAlpha(fidA) + rewards[r][s]);
                    ioForest.addAlpha(fid01, val);

                    fidA.comp = one;
                    // alpha[s][t][1][1]
                    val = Util.logsumexp(ioForest.getAlpha(fid11),
                            ioForest.getBeta(fidB.setIndex(t, r, one, zero)) + ioForest.getAlpha(fidA));
                    ioForest.addAlpha(fid11, val);
                }

                // alpha[s][t][0][0]
                for (short r = t; r < length; ++r) {
                    double val = Util.logsumexp(ioForest.getAlpha(fid00),
                            ioForest.getBeta(fidB.setIndex(t, r, zero, one))
                                    + ioForest.getAlpha(fidA.setIndex(s, r, zero, one)));
                    ioForest.addAlpha(fid00, val);
                }
                // alpha[s][t][1][0]
                for (short r = 0; r <= s; ++r) {
                    double val = Util.logsumexp(ioForest.getAlpha(fid10),
                            ioForest.getBeta(fidB.setIndex(r, s, one, one))
                                    + ioForest.getAlpha(fidA.setIndex(r, t, one, one)));
                    ioForest.addAlpha(fid10, val);
                }
            }
        }
    }

    @Override
    public Marginal calcMarginals(Manager manager, ParserModel model, ObjectReader in)
            throws TrainingException, IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }
}
