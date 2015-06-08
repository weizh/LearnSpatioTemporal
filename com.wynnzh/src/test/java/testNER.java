

import edu.cmu.lti.weizh.docmodel.Sentence;
import edu.cmu.lti.weizh.docmodel.Word;
import edu.cmu.lti.weizh.eval.Prediction;
import edu.cmu.lti.weizh.nlp.ner.PercTaggerNerOnf;
import edu.cmu.lti.weizh.nlp.pos.PercTaggerPosOnf;
import edu.cmu.lti.weizh.nlp.tokenizer.EuroLangTwokenizer;

public class testNER {

	public static void main(String argv[]) throws Exception {

		String s = "Dozens of Palestinians were wounded in scattered clashes "
				+ "in the West Bank and Gaza Strip, Wednesday, despite the Sharm el-Sheikh truce accord.";
		Sentence sent = new Sentence(s);
		EuroLangTwokenizer.tokenize(sent);
		PercTaggerPosOnf postagger = new PercTaggerPosOnf("src/main/resources/trainedModels/ONF_CNNTrain_POS_PERC_100_NegInf_basic.trainer");
		PercTaggerNerOnf nertagger = new PercTaggerNerOnf("src/main/resources/trainedModels/ONF_VOATrain_NER_PERC_100_NegInf_basic.trainer");
		System.out.println("Models loaded.");
		postagger.tag(sent);
		postagger.tagWithLabelDistribution(sent);
		nertagger.tag(sent);
		nertagger.tagWithLabelDistribution(sent);

		for (Word w : sent.getWords()) {
			System.out.println("--------------------------   " + w.getWord()+ "  ------------------------");
			Prediction pd = w.getPosPDist();
			Prediction nd = w.getNerPDist();
			System.out.println("POS: Viterbi:"+ w.getPartOfSpeech()+" SumProduct:"+ pd.getBestCandidateName());
			System.out.println("POS Sum product entropy:"+ pd.getEntropy());
			System.out.println("NER: Viterbi:"+ w.getEntityType()+" SumProduct:"+ nd.getBestCandidateName());
			System.out.println("NER Sum product entropy:"+ nd.getEntropy());
		}
	}
}
