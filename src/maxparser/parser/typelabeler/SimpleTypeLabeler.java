package maxparser.parser.typelabeler;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;
import maxparser.parser.typelabeler.featgen.SimpleLabeledFeatureGenerator;
import maxparser.io.ObjectReader;
import maxparser.io.ObjectWriter;

public class SimpleTypeLabeler extends TypeLabeler {
    protected double[][][][] nt_probs = null;
    protected int[][] dep_types = null;
    
    public SimpleTypeLabeler() {
        featGen = new SimpleLabeledFeatureGenerator();
    }
    
    @Override
    public void init(int maxLength, int type_size) {
        nt_probs = new double[maxLength][type_size][2][2];
    }
    
    @Override
    public void genLabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv) {
        for(int i = 0; i < inst.length(); ++i){
            if(inst.heads[i] == -1){
                continue;
            }
            
            boolean attR = i < inst.heads[i] ? false : true;
            
            //single label features
            ((SimpleLabeledFeatureGenerator) featGen).addChildSingleLabelFeatures(inst, i, inst.deprelIds[i], attR, model, fv);
            ((SimpleLabeledFeatureGenerator) featGen).addParentSingleLabelFeatures(inst, inst.heads[i], inst.deprelIds[i], attR, model, fv);
        }
    }
    
    @Override
    public void writeLabeledInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) throws IOException {
        int length = inst.length();
        out.writeInt(length);
        out.writeInt(-4);
        
        int type_size = model.typeSize();
        for(int w = 0; w < length; ++w){
            for(int t = 0; t < type_size; ++t){
                //attR = true
                FeatureVector fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addChildSingleLabelFeatures(inst, w, t, true, model, fv);
                out.writeObject(fv.keys());
                
                fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addParentSingleLabelFeatures(inst, w, t, true, model, fv);
                out.writeObject(fv.keys());
                
                //attR = false
                fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addChildSingleLabelFeatures(inst, w, t, false, model, fv);
                out.writeObject(fv.keys());
                
                fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addParentSingleLabelFeatures(inst, w, t, false, model, fv);
                out.writeObject(fv.keys());
            }
        }
        out.writeInt(-3);
    }
    
    @Override
    public int readLabeledInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException {
        int length = in.readInt();
        int last = in.readInt();
        if(last != -4){
            throw new IOException("last number is not equal to -4");
        }
        int type_size = model.typeSize();
        for(int w = 0; w < length; ++w){
            for(int t = 0; t < type_size; ++t){
                //dir = 0;
                FeatureVector fv = new FeatureVector((int[]) in.readObject());
                nt_probs[w][t][0][0] = model.getScore(fv);
                fv = new FeatureVector((int[]) in.readObject());
                nt_probs[w][t][0][1] = model.getScore(fv);
                
                //dir = 1;
                fv = new FeatureVector((int[]) in.readObject());
                nt_probs[w][t][1][0] = model.getScore(fv);
                fv = new FeatureVector((int[]) in.readObject());
                nt_probs[w][t][1][1] = model.getScore(fv);
            }
        }
        last = in.readInt();
        if(last != -3){
            throw new IOException("last number is not equal to -3");
        }
        return length;
    }
    
    @Override
    public void fillLabeledFeatureVector(DependencyInstance inst, ParserModel model) {
        int length = inst.length();
        int type_size = model.typeSize();
        for(int w = 0; w < length; ++w){
            for(int t = 0; t < type_size; ++t){
                //attR = true
                FeatureVector fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addChildSingleLabelFeatures(inst, w, t, true, model, fv);
                nt_probs[w][t][0][0] = model.getScore(fv);
                
                fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addParentSingleLabelFeatures(inst, w, t, true, model, fv);
                nt_probs[w][t][0][1] = model.getScore(fv);
                
                //attR = false
                fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addChildSingleLabelFeatures(inst, w, t, false, model, fv);
                nt_probs[w][t][1][0] = model.getScore(fv);
                
                fv = new FeatureVector();
                ((SimpleLabeledFeatureGenerator) featGen).addParentSingleLabelFeatures(inst, w, t, false, model, fv);
                nt_probs[w][t][1][1] = model.getScore(fv);
            }
        }
    }
    
    @Override
    public double getLabeledScore(IndexTuple itemId) {
        SingleEdgeIndexTuple id = (SingleEdgeIndexTuple) itemId;
        int dir = id.par < id.ch ? 0 : 1;
        return nt_probs[id.par][id.type][dir][1] + nt_probs[id.ch][id.type][dir][0];
    }

    @Override
    public void getType(DependencyInstance inst, IndexTuple itemId, ParserModel model) {
        SingleEdgeIndexTuple id = (SingleEdgeIndexTuple) itemId;
        id.type = dep_types[id.par][id.ch];
    }
    
    @Override
    public void getTypes(int length, ParserModel model) {
        dep_types = new int[length][length];
        SingleEdgeIndexTuple index = new SingleEdgeIndexTuple();
        for(int i = 0; i < length; ++i){
            index.par = i;
            for(int j = 0; j < length; ++j){
                index.ch = j;
                if(i == j){
                    dep_types[i][j] = 0;
                    continue;
                }
                int wh = -1;
                double best = Double.NEGATIVE_INFINITY;
                int type_size = model.typeSize();
                for(int t = 0; t < type_size; ++t){
                    index.type = t;
                    double score = getLabeledScore(index);
                    if(score > best){
                        wh = t;
                        best = score;
                    }
                }
                dep_types[i][j] = wh;
            }
        }
    }
    
    @Override
    public TypeLabeler clone(int size, int type_size) {
        SimpleTypeLabeler typeLabeler = new SimpleTypeLabeler();
        typeLabeler.init(size, type_size);
        return typeLabeler;
    }
}
