package com.tictac.controller;

import com.tictac.model.chess.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class ChessController {
    
    @GetMapping("/chess")
    public String chess(Model model, HttpSession session) {
        Board board = (Board) session.getAttribute("chessBoard");
        if (board == null) {
            board = new Board();
            session.setAttribute("chessBoard", board);
        }
        
        // Add username to model
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        
        model.addAttribute("board", board.getSquares());
        model.addAttribute("currentPlayer", board.getCurrentPlayer());
        model.addAttribute("capturedByWhite", board.getCapturedByWhite());
        model.addAttribute("capturedByBlack", board.getCapturedByBlack());
        model.addAttribute("possibleMoves", new ArrayList<>());
        
        // Add game state information
        if (board.isCheckmate()) {
            model.addAttribute("gameState", "Checkmate! " + 
                (board.getCurrentPlayer().equals("white") ? "Black" : "White") + " wins!");
        } else if (board.isStalemate()) {
            model.addAttribute("gameState", "Stalemate! Game is a draw.");
        } else if (board.isInCheck(board.getCurrentPlayer())) {
            model.addAttribute("gameState", "Check!");
        } else {
            model.addAttribute("gameState", "");
        }
        
        return "chess";
    }
    
    @GetMapping("/chess/moves")
    @ResponseBody
    public List<int[]> getPossibleMoves(@RequestParam int row, @RequestParam int col, HttpSession session) {
        Board board = (Board) session.getAttribute("chessBoard");
        if (board != null) {
            return board.getPossibleMoves(row, col);
        }
        return new ArrayList<>();
    }
    
    @PostMapping("/chess/move")
    public String makeMove(@RequestParam int fromRow, @RequestParam int fromCol,
                         @RequestParam int toRow, @RequestParam int toCol,
                         HttpSession session) {
        Board board = (Board) session.getAttribute("chessBoard");
        if (board != null) {
            board.makeMove(fromRow, fromCol, toRow, toCol);
        }
        return "redirect:/chess";
    }
    
    @PostMapping("/chess/new")
    public String newGame(HttpSession session) {
        Board newBoard = new Board();
        session.setAttribute("chessBoard", newBoard);
        return "redirect:/chess";
    }
} 