package com.nice.avishkar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;



public class Solution {
	private List<ConstituencyResult> constituencyResultCalculate(Map<String, List<CandidateVotes>> candidateMap){
		List<ConstituencyResult> result = new ArrayList<>();
		for (Map.Entry<String, List<CandidateVotes>> entry : candidateMap.entrySet()) {
			String constituencyName = entry.getKey();
			List<CandidateVotes> candidateVotesList = entry.getValue();

			String winner = "";
			long maxVotes = 0;
			long secondMaxVotes = 0;

			for (CandidateVotes candidateVotes : candidateVotesList) {
				long votes = candidateVotes.getVotes();
				String candidateName = candidateVotes.getCandidateName();

				if (!candidateName.equals("NOTA") && votes >= maxVotes) {
					secondMaxVotes = maxVotes;
					winner = candidateName;
					maxVotes = votes;
				} else if (!candidateName.equals("NOTA") && votes > secondMaxVotes) {
					secondMaxVotes = votes;
				}
			}

			if (maxVotes == secondMaxVotes) {
				winner = "NO_WINNER";
			}

			candidateVotesList.sort((a, b) -> {
				int compareVotes = Long.compare(b.getVotes(), a.getVotes());

				if (compareVotes == 0) {
					return a.getCandidateName().compareTo(b.getCandidateName());
				}

				return compareVotes;
			});


			ConstituencyResult constituencyResult = new ConstituencyResult();
			constituencyResult.setConstituencyName(constituencyName);
			constituencyResult.setWinnerName(winner);
			constituencyResult.setCandidateList(candidateVotesList);

			result.add(constituencyResult);
		}
		return result;

	}

	public ElectionResult execute(Path candidateFile, Path votingFile) {
		Map<String, List<CandidateVotes>> candidateMap = readCandidates(candidateFile);
		calculateVotes(votingFile , candidateMap);

		List<ConstituencyResult> constituencyResult = constituencyResultCalculate(candidateMap);


		ElectionResult resultData = new ElectionResult(constituencyResult);

		return resultData;
	}

	private Map<String, List<CandidateVotes>> readCandidates(Path candidateFile) {


		Map<String, List<CandidateVotes>> candidateMap = new HashMap<>();

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(candidateFile.toFile()));
			while((line = reader.readLine())!=null){
				String[] row = line.split(",");

				String constituency = row[0].trim();
				String candidateName = row[1].trim();
				candidateMap.putIfAbsent(constituency, new ArrayList<>());
				candidateMap.get(constituency).add(new CandidateVotes(candidateName , 0));

			}

			for(List<CandidateVotes> candidatesMap : candidateMap.values()){
				candidatesMap.add( new CandidateVotes("NOTA",0));
			}
		}
		catch (Exception e){
			System.out.println(e.toString());
			e.printStackTrace();
		}


		return candidateMap;
	}

	private void removeVoteFromCandidate(String constituency, String candidateName, Map<String, List<CandidateVotes>> candidateMap) {
		List<CandidateVotes> constituencyVotes = candidateMap.get(constituency);
		for (CandidateVotes candidate : constituencyVotes) {
			if (candidate.getCandidateName().equals(candidateName)) {
				candidate.setVotes(candidate.getVotes() - 1);
				break;
			}
		}
	}

	private void addVoteFromCandidate(String constituency, String candidateName, Map<String, List<CandidateVotes>> candidateMap) {
		List<CandidateVotes> constituencyVotes = candidateMap.get(constituency);
		for (CandidateVotes candidate : constituencyVotes) {
			if (candidate.getCandidateName().equals(candidateName)) {
				candidate.setVotes(candidate.getVotes() + 1);
				break;
			}
		}
	}

	private void calculateVotes(Path votingFile , Map<String, List<CandidateVotes>> candidateMap){

		Map<String , SimpleEntry<String, String>> voted = new HashMap<>() ;
		Set<String> frauds = new HashSet<>();

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(votingFile.toFile()));
			while((line = reader.readLine())!=null){

				String[] row = line.split(",");

				String voterId = row[0].trim();
				String constituency = row[1].trim();
				String candidateName = row[3].trim();

				if( voted.containsValue(voterId)){
					if(frauds.contains(voterId)){
						continue;
					}
					else{
						frauds.add(voterId);
						// remove one vote from the candidate
						SimpleEntry<String,String> candidate = voted.get(voterId);
						removeVoteFromCandidate(candidate.getKey() , candidate.getValue() , candidateMap);
					}
				}else{
					SimpleEntry<String, String> candidateVote = new SimpleEntry<>(constituency, candidateName);
					voted.put(voterId , candidateVote);
					addVoteFromCandidate(constituency , candidateName , candidateMap);
				}
			}

		}catch (Exception e){
			System.out.println("Error reading file");
			e.printStackTrace();
		}
	}



}

