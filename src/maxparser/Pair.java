package maxparser;

public class Pair<T1, T2> {
	public T1 first;
	public T2 second;
	
	public Pair(T1 first, T2 second){
		super();
		this.first = first;
		this.second = second;
	}
	
	public int hashCode(){
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;
		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}
	
	public boolean equals(Object other){
		if (other instanceof Pair){
			Pair<T1, T2> otherPair = (Pair<T1, T2>) other;
			return ((this.first == otherPair.first || (this.first != null && otherPair.first != null && this.first.equals(otherPair.first))) 
					&& 
					(this.second == otherPair.second || (this.second != null && otherPair.second != null && this.second.equals(otherPair.second))));
		}
		return false;
	}
	
	public String toString(){
		return "(" + first + ", " + second + ")";
	}
}
