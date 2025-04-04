package com.tictac.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class GameController {
    
    private final Map<String, GameState> gameStates = new ConcurrentHashMap<>();
    
    @GetMapping("/tictac")
    public String tictac(Model model, HttpSession session) {
        String sessionId = session.getId();
        GameState gameState = gameStates.computeIfAbsent(sessionId, k -> new GameState());
        
        updateGameModel(model, gameState);
        return "tictac";
    }
    
    @PostMapping("/tictac/move")
    public String makeMove(@RequestParam int row, @RequestParam int col, 
                         Model model, HttpSession session) {
        String sessionId = session.getId();
        GameState gameState = gameStates.get(sessionId);
        
        if (gameState == null || gameState.isGameOver()) {
            return "redirect:/tictac";
        }
        
        if (isValidMove(row, col, gameState.getBoard())) {
            makeMove(row, col, gameState);
            checkGameStatus(gameState);
        }
        
        updateGameModel(model, gameState);
        return "tictac";
    }
    
    @PostMapping("/tictac/new")
    public String newGame(HttpSession session) {
        String sessionId = session.getId();
        gameStates.put(sessionId, new GameState());
        return "redirect:/tictac";
    }
    
    @PostMapping("/tictac/reset")
    public String resetGame(HttpSession session) {
        String sessionId = session.getId();
        GameState gameState = gameStates.get(sessionId);
        if (gameState != null) {
            gameState.resetBoard();
        }
        return "redirect:/tictac";
    }
    
    private void updateGameModel(Model model, GameState gameState) {
        model.addAttribute("board", gameState.getBoard());
        model.addAttribute("currentPlayer", gameState.getCurrentPlayer());
        model.addAttribute("gameOver", gameState.isGameOver());
        model.addAttribute("message", gameState.getMessage());
        model.addAttribute("wins", 0);
        model.addAttribute("draws", gameState.getDraws());
        model.addAttribute("canUndo", false);
        model.addAttribute("scoreX", gameState.getScoreX());
        model.addAttribute("scoreO", gameState.getScoreO());
        model.addAttribute("winningLine", gameState.getWinningLine());
    }
    
    private boolean isValidMove(int row, int col, char[][] board) {
        return row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col] == ' ';
    }
    
    private void makeMove(int row, int col, GameState gameState) {
        gameState.getBoard()[row][col] = gameState.getCurrentPlayer();
    }
    
    private void checkGameStatus(GameState gameState) {
        char[][] board = gameState.getBoard();
        String winningLine = checkWin(board);
        if (winningLine != null) {
            gameState.setWinningLine(winningLine);
            gameState.setMessage("Player " + gameState.getCurrentPlayer() + " wins!");
            gameState.setGameOver(true);
            if (gameState.getCurrentPlayer() == 'X') {
                gameState.setScoreX(gameState.getScoreX() + 1);
            } else {
                gameState.setScoreO(gameState.getScoreO() + 1);
            }
        } else if (isBoardFull(board)) {
            gameState.setMessage("It's a draw!");
            gameState.setGameOver(true);
            gameState.setDraws(gameState.getDraws() + 1);
        } else {
            gameState.setCurrentPlayer(gameState.getCurrentPlayer() == 'X' ? 'O' : 'X');
        }
    }
    
    private static class GameState {
        private final char[][] board = new char[3][3];
        private char currentPlayer = 'X';
        private String message;
        private boolean gameOver;
        private int scoreX = 0;
        private int scoreO = 0;
        private int draws = 0;
        private String winningLine = null;
        
        public GameState() {
            resetBoard();
        }
        
        public void resetBoard() {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    board[i][j] = ' ';
                }
            }
            currentPlayer = 'X';
            message = null;
            gameOver = false;
            winningLine = null;
        }
        
        public char[][] getBoard() { return board; }
        public char getCurrentPlayer() { return currentPlayer; }
        public String getMessage() { return message; }
        public boolean isGameOver() { return gameOver; }
        public int getScoreX() { return scoreX; }
        public int getScoreO() { return scoreO; }
        public int getDraws() { return draws; }
        public String getWinningLine() { return winningLine; }
        
        public void setCurrentPlayer(char player) { this.currentPlayer = player; }
        public void setMessage(String message) { this.message = message; }
        public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
        public void setScoreX(int scoreX) { this.scoreX = scoreX; }
        public void setScoreO(int scoreO) { this.scoreO = scoreO; }
        public void setDraws(int draws) { this.draws = draws; }
        public void setWinningLine(String winningLine) { this.winningLine = winningLine; }
    }
    
    private String checkWin(char[][] board) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return i + "-0," + i + "-2";  // Return start and end positions
            }
        }
        
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != ' ' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return "0-" + i + ",2-" + i;  // Return start and end positions
            }
        }
        
        // Check diagonals
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return "0-0,2-2";  // Return start and end positions
        }
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return "0-2,2-0";  // Return start and end positions
        }
        return null;
    }
    
    private boolean isBoardFull(char[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }
} 