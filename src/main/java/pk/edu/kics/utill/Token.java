package pk.edu.kics.utill;

public class Token {
	
	public String word ;
	public String lemma;
	public String pos;
	public String depLabel;
	public Token head;
	
	public Token()
	{
		
	}
	public Token(String word, String lemma, String pos, String depLabel) {
		super();
		this.word = word;
		this.lemma = lemma;
		this.pos = pos;
		this.depLabel = depLabel;
		
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getDepLabel() {
		return depLabel;
	}
	public void setDepLabel(String depLabel) {
		this.depLabel = depLabel;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public Token getHead() {
		return head;
	}
	public void setHead(Token head) {
		this.head = head;
	}
	public void iamtoken() {
		System.out.println(word);
		System.out.println(pos);
		System.out.println(depLabel);
		System.out.println(lemma);
		if(head != null)
		{
			iamtoken(head);
		}
		
	}
	public void iamtoken(Token token) {
		if(token.getHead() != null)
		{
		System.out.println(token.word);
		System.out.println(token.pos);
		System.out.println(token.depLabel);
		System.out.println(token.lemma);
		System.out.println("\n\n\n\n");
		iamtoken(token.getHead());
		}
		
	}

}
