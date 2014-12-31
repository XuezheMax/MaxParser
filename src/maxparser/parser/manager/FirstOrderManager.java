package maxparser.parser.manager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import maxparser.DependencyInstance;
import maxparser.model.ParserModel;

public class FirstOrderManager extends Manager{

	@Override
	protected void writeInstance(DependencyInstance inst, ObjectOutputStream out, ParserModel model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DependencyInstance readInstance(ObjectInputStream in, ParserModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(int maxLength) {
		// TODO Auto-generated method stub
		
	}

}
