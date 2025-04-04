package com.tictac.model.chess;

import lombok.Data;

@Data
public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private Piece piece;
    
    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.piece = piece;
    }
} 