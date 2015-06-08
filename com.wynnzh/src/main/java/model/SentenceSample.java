package model;

import java.io.Serializable;
import java.util.Map;

import edu.cmu.lti.weizh.docmodel.Sentence;

public class SentenceSample implements Map.Entry<String, Sentence>, Serializable {

	private final String forWord;
    private Sentence sent;
    
    public SentenceSample(String forWord, Sentence sent) {
        this.forWord = forWord;
        this.sent = sent;
    }
    
	@Override
	public String getKey() {
		
		return forWord;
	}

	@Override
	public Sentence getValue() {
		return sent;
	}

	@Override
	public Sentence setValue(Sentence sentence) {
		sent = sentence;
		return sent;
	}

}
