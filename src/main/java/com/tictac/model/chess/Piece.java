package com.tictac.model.chess;

import lombok.Data;

@Data
public class Piece {
    private String type;
    private String color;
    private int row;
    private int col;
    
    public Piece(String type, String color, int row, int col) {
        this.type = type;
        this.color = color;
        this.row = row;
        this.col = col;
    }
    
    public boolean isValidMove(int newRow, int newCol, Board board) {
        // Basic validation, specific piece movement logic will be in Board class
        return newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8;
    }
} 