package edu.ucsd.cvalue;

import java.util.ArrayList;
import java.util.List;

public class CValueMain {
	public static void main(String[] args) {
		List<CValueData> cValueData = new ArrayList<CValueData>();
		String phrase = "ADENOID CYSTIC BASAL CELL CARCINOMA";
		CValueData cData = new CValueData(phrase, 5, 5);
		cValueData.add(cData);
		
		phrase = "CYSTIC BASAL CELL CARCINOMA";
		cData = new CValueData(phrase, 11, 4);
		cValueData.add(cData);
		
		phrase = "ULCERATED BASAL CELL CARCINOMA";
		cData = new CValueData(phrase, 7, 4);
		cValueData.add(cData);
		
		phrase = "RECURRENT BASAL CELL CARCINOMA";
		cData = new CValueData(phrase, 5, 4);
		cValueData.add(cData);
		
		phrase = "CIRCUMSCRIBED BASAL CELL CARCINOMA";
		cData = new CValueData(phrase, 3, 4);
		cValueData.add(cData);
		
		phrase = "BASAL CELL CARCINOMA";
		cData = new CValueData(phrase, 984, 3);
		cValueData.add(cData);
		
		CValueCalculator.calculate(cValueData);
		
		for(CValueData data : cValueData) {
			System.out.println("C-Value: " + data.getCValue());
		}
	}
}
