package com.nice.avishkar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;



public class Solution {
	private List<ConstituencyResult> constituencyResultCalculate(Map<String, Map<String,CandidateVotes>> candidateMap){
		List<ConstituencyResult> result = new ArrayList<>();
		for(Map.Entry<String, Map<String,CandidateVotes>> entry: candidateMap.entrySet()){

			String winner = "";
			long maxVotes = 0;
			long secondMaxVotes = 0;
			List<CandidateVotes> votesList = new ArrayList<>();
			ConstituencyResult constituency = new ConstituencyResult();

			for(Map.Entry<String, CandidateVotes> candidateEntry: entry.getValue().entrySet()){
				CandidateVotes candidateVotes = candidateEntry.getValue();
				long votes = candidateVotes.getVotes();
				String candidateName = candidateVotes.getCandidateName();

				if (!candidateVotes.getCandidateName().equals("NOTA") &&  votes >= maxVotes) {
					secondMaxVotes =  maxVotes ;
					winner = candidateName;
					maxVotes = votes;
				} else if (!candidateVotes.getCandidateName().equals("NOTA") && votes > secondMaxVotes) {
					secondMaxVotes = votes;
				}

				votesList.add(candidateVotes);
			}
			if (maxVotes==secondMaxVotes){
				winner = "NO_WINNER";
			}
			votesList.sort((c1, c2) -> Long.compare(c2.getVotes(), c1.getVotes()));
			constituency.setConstituencyName(entry.getKey());
			constituency.setCandidateList(votesList);
			constituency.setWinnerName(winner);

			result.add(constituency);

		}
		return result;
	}


	public ElectionResult execute(Path candidateFile, Path votingFile) {
		Map<String, Map<String,CandidateVotes>> candidateMap = readCandidates(candidateFile);
		calculateVotes(votingFile , candidateMap);

		List<ConstituencyResult> constituencyResult = constituencyResultCalculate(candidateMap);



		ElectionResult resultData = new ElectionResult(constituencyResult);

		return resultData;
	}

	private Map<String, Map<String,CandidateVotes>> readCandidates(Path candidateFile) {


		Map<String, Map<String,CandidateVotes>> candidateMap = new HashMap<>();

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(candidateFile.toFile()));
			while((line = reader.readLine())!=null){
				String[] row = line.split(",");

				String constituency = row[0].trim();
				String candidateName = row[1].trim();
				candidateMap.putIfAbsent(constituency, new HashMap<>());
				candidateMap.get(constituency).put(candidateName , new CandidateVotes(candidateName , 0));

			}

			for(Map<String,CandidateVotes> constituencyMap : candidateMap.values()){
				constituencyMap.put("NOTA", new CandidateVotes("NOTA",0));
			}
		}
		catch (Exception e){
			System.out.println(e.toString());
			e.printStackTrace();
		}


		return candidateMap;
	}

	private void removeVoteFromCandidate(String constituency, String candidateName, Map<String, Map<String,CandidateVotes>> candidateMap) {
		Map<String,CandidateVotes> constituencyVotes = candidateMap.get(constituency);
		CandidateVotes candidate = constituencyVotes.get(candidateName);
		candidate.setVotes(candidate.getVotes()-1);
	}

	private void addVoteFromCandidate(String constituency, String candidateName, Map<String, Map<String,CandidateVotes>> candidateMap) {
		Map<String,CandidateVotes> constituencyVotes = candidateMap.get(constituency);
		CandidateVotes candidate = constituencyVotes.get(candidateName);
		candidate.setVotes(candidate.getVotes()+1);
	}

	private void calculateVotes(Path votingFile , Map<String, Map<String,CandidateVotes>> candidateMap){

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

	public static void main(String[] args) {

		System.out.println("hello Sahil");

		String votingFilePath = "src/main/resources/votingFile.csv";
		String candidateFilePath =  "src/main/resources/candidateFile.csv";


		// Create Path objects for the candidate file and voting file
		Path candidatePath = Paths.get(candidateFilePath);
		Path votingPath = Paths.get(votingFilePath);
		Solution s = new Solution();
		s.execute(candidatePath , votingPath);
	}

}

