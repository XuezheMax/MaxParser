package maxparser.model;

import gnu.trove.map.hash.TObjectIntHashMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Alphabet implements Serializable{
	TObjectIntHashMap<String> map;
	
	int numEntries;

	boolean growthStopped = false;

	public Alphabet(int capacity) {
		this.map = new TObjectIntHashMap<String>(capacity);
		numEntries = 0;
	}

	public Alphabet() {
	this(10000);
	}

	/** Return -1 if entry isn't present. */
	public int lookupIndex(String entry) {
		if (entry == null) {
			throw new IllegalArgumentException("Can't lookup \"null\" in an Alphabet.");
		}
		
		int ret = map.get(entry);
		
		if (ret == -1 && !growthStopped) {
			ret = numEntries;
			map.put(entry, ret);
			numEntries++;
		}
		return ret;
	}
	
	public String[] toArray() {
		return (String[])map.keys();
	}

	public boolean contains(String entry) {
		return map.contains(entry);
	}

	public int size() {
		return numEntries;
	}

	public void stopGrowth() {
		growthStopped = true;
		map.compact();
	}

	public void allowGrowth() {
		growthStopped = false;
	}

	public boolean growthStopped() {
		return growthStopped;
	}

	// Serialization

	private static final long serialVersionUID = 1;

	private static final int CURRENT_SERIAL_VERSION = 0;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);
		out.writeInt(numEntries);
		out.writeObject(map);
		out.writeBoolean(growthStopped);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		if(version != CURRENT_SERIAL_VERSION){
			System.err.println("version error:");
			System.err.println("current version: " + CURRENT_SERIAL_VERSION);
			System.err.println("model version: " + version);
			System.exit(1);
		}
		numEntries = in.readInt();
		map = (TObjectIntHashMap<String>) in.readObject();
		growthStopped = in.readBoolean();
	}
}
