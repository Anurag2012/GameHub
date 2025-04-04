package com.tictac.model.chess;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] squares = new Piece[8][8];
    private String currentPlayer = "white";
    private List<Move> moveHistory = new ArrayList<>();
    private List<Piece> capturedByWhite = new ArrayList<>();
    private List<Piece> capturedByBlack = new ArrayList<>();
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean[] whiteRooksMoved = {false, false}; // queenside, kingside
    private boolean[] blackRooksMoved = {false, false}; // queenside, kingside
    private int enPassantTarget = -1; // column where en passant is possible, -1 if none

    public Board() {
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize pawns
        for (int col = 0; col < 8; col++) {
            squares[1][col] = new Piece("pawn", "black", 1, col);
            squares[6][col] = new Piece("pawn", "white", 6, col);
        }
        
        // Initialize other pieces
        String[] pieces = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};
        for (int col = 0; col < 8; col++) {
            squares[0][col] = new Piece(pieces[col], "black", 0, col);
            squares[7][col] = new Piece(pieces[col], "white", 7, col);
        }
    }

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = squares[fromRow][fromCol];
        if (piece == null || !piece.getColor().equals(currentPlayer)) {
            return false;
        }

        // Check if move is valid
        if (isValidMove(fromRow, fromCol, toRow, toCol)) {
            // Store the move for en passant
            int oldEnPassantTarget = enPassantTarget;
            enPassantTarget = -1; // Reset en passant target
            
            // Handle special moves
            if (piece.getType().equals("pawn")) {
                // Check for en passant
                if (Math.abs(fromCol - toCol) == 1 && squares[toRow][toCol] == null) {
                    // This is an en passant capture
                    int capturedPawnRow = fromRow;
                    Piece capturedPawn = squares[capturedPawnRow][toCol];
                    if (piece.getColor().equals("white")) {
                        capturedByWhite.add(capturedPawn);
                    } else {
                        capturedByBlack.add(capturedPawn);
                    }
                    squares[capturedPawnRow][toCol] = null;
                }
                
                // Set en passant target for next move
                if (Math.abs(fromRow - toRow) == 2) {
                    enPassantTarget = fromCol;
                }
                
                // Check for promotion
                if (toRow == 0 || toRow == 7) {
                    piece.setType("queen"); // Auto-promote to queen
                }
            }
            
            // Handle castling
            if (piece.getType().equals("king") && Math.abs(fromCol - toCol) == 2) {
                // This is a castling move
                int rookCol = toCol > fromCol ? 7 : 0;
                int newRookCol = toCol > fromCol ? toCol - 1 : toCol + 1;
                Piece rook = squares[fromRow][rookCol];
                squares[fromRow][newRookCol] = rook;
                squares[fromRow][rookCol] = null;
                rook.setCol(newRookCol);
            }
            
            // Update king/rook moved status
            if (piece.getType().equals("king")) {
                if (piece.getColor().equals("white")) {
                    whiteKingMoved = true;
                } else {
                    blackKingMoved = true;
                }
            } else if (piece.getType().equals("rook")) {
                if (piece.getColor().equals("white")) {
                    if (fromCol == 0) whiteRooksMoved[0] = true;
                    if (fromCol == 7) whiteRooksMoved[1] = true;
                } else {
                    if (fromCol == 0) blackRooksMoved[0] = true;
                    if (fromCol == 7) blackRooksMoved[1] = true;
                }
            }
            
            // Check if there's a piece to capture
            Piece capturedPiece = squares[toRow][toCol];
            if (capturedPiece != null) {
                if (piece.getColor().equals("white")) {
                    capturedByWhite.add(capturedPiece);
                } else {
                    capturedByBlack.add(capturedPiece);
                }
            }
            
            // Make the move
            Move move = new Move(fromRow, fromCol, toRow, toCol, piece);
            moveHistory.add(move);
            
            squares[toRow][toCol] = piece;
            squares[fromRow][fromCol] = null;
            piece.setRow(toRow);
            piece.setCol(toCol);
            
            // If the player is in check, verify that this move gets them out of check
            if (isInCheck(currentPlayer)) {
                // Undo the move
                squares[fromRow][fromCol] = piece;
                squares[toRow][toCol] = capturedPiece;
                piece.setRow(fromRow);
                piece.setCol(fromCol);
                enPassantTarget = oldEnPassantTarget;
                if (capturedPiece != null) {
                    if (piece.getColor().equals("white")) {
                        capturedByWhite.remove(capturedByWhite.size() - 1);
                    } else {
                        capturedByBlack.remove(capturedByBlack.size() - 1);
                    }
                }
                return false;
            }
            
            currentPlayer = currentPlayer.equals("white") ? "black" : "white";
            return true;
        }
        return false;
    }
    
    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = squares[fromRow][fromCol];
        if (piece == null) return false;
        
        // Basic validation
        if (!piece.isValidMove(toRow, toCol, this)) return false;
        
        // Check if destination has a piece of the same color
        if (squares[toRow][toCol] != null && 
            squares[toRow][toCol].getColor().equals(piece.getColor())) {
            return false;
        }

        // Special move validations
        if (piece.getType().equals("king") && Math.abs(fromCol - toCol) == 2) {
            return isValidCastling(fromRow, fromCol, toRow, toCol);
        }
        
        if (piece.getType().equals("pawn")) {
            // Check for en passant
            if (Math.abs(fromCol - toCol) == 1 && squares[toRow][toCol] == null) {
                int capturedPawnRow = fromRow;
                Piece capturedPawn = squares[capturedPawnRow][toCol];
                return capturedPawn != null && 
                       capturedPawn.getType().equals("pawn") && 
                       capturedPawn.getColor().equals(piece.getColor().equals("white") ? "black" : "white") &&
                       enPassantTarget == toCol;
            }
            
            // Check for normal pawn moves
            if (fromCol == toCol) {
                // Forward move
                if (squares[toRow][toCol] != null) return false;
                if (piece.getColor().equals("white")) {
                    if (fromRow - toRow == 1) return true;
                    if (fromRow == 6 && toRow == 4 && squares[5][toCol] == null) return true;
                } else {
                    if (toRow - fromRow == 1) return true;
                    if (fromRow == 1 && toRow == 3 && squares[2][toCol] == null) return true;
                }
            } else {
                // Capture move
                if (squares[toRow][toCol] == null) return false;
                if (piece.getColor().equals("white")) {
                    return fromRow - toRow == 1 && Math.abs(fromCol - toCol) == 1;
                } else {
                    return toRow - fromRow == 1 && Math.abs(fromCol - toCol) == 1;
                }
            }
        }

        // For all other pieces, check if the path is clear
        if (piece.getType().equals("rook") || piece.getType().equals("bishop") || 
            piece.getType().equals("queen")) {
            int rowStep = Integer.compare(toRow, fromRow);
            int colStep = Integer.compare(toCol, fromCol);
            int currentRow = fromRow + rowStep;
            int currentCol = fromCol + colStep;
            
            while (currentRow != toRow || currentCol != toCol) {
                if (squares[currentRow][currentCol] != null) return false;
                currentRow += rowStep;
                currentCol += colStep;
            }
        }

        return true;
    }
    
    private boolean isValidCastling(int fromRow, int fromCol, int toRow, int toCol) {
        Piece king = squares[fromRow][fromCol];
        if (king == null || !king.getType().equals("king")) return false;
        
        // Check if king has moved
        if (king.getColor().equals("white") && whiteKingMoved) return false;
        if (king.getColor().equals("black") && blackKingMoved) return false;
        
        // Check if path is clear and not under attack
        int direction = toCol > fromCol ? 1 : -1;
        int rookCol = toCol > fromCol ? 7 : 0;
        int rookIndex = toCol > fromCol ? 1 : 0;
        
        // Check if rook has moved
        if (king.getColor().equals("white") && whiteRooksMoved[rookIndex]) return false;
        if (king.getColor().equals("black") && blackRooksMoved[rookIndex]) return false;
        
        // Check if squares between king and rook are empty
        for (int col = fromCol + direction; col != rookCol; col += direction) {
            if (squares[fromRow][col] != null) return false;
        }
        
        // Check if king is in check
        if (isInCheck(king.getColor())) return false;
        
        // Check if squares the king moves through are under attack
        for (int col = fromCol; col != toCol + direction; col += direction) {
            // Temporarily move the king to check if the square is under attack
            squares[fromRow][fromCol] = null;
            squares[fromRow][col] = king;
            boolean isUnderAttack = isInCheck(king.getColor());
            squares[fromRow][col] = null;
            squares[fromRow][fromCol] = king;
            
            if (isUnderAttack) return false;
        }
        
        return true;
    }
    
    public boolean isInCheck(String color) {
        // Find the king's position
        int kingRow = -1, kingCol = -1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.getType().equals("king") && piece.getColor().equals(color)) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        
        if (kingRow == -1) return false; // King not found (shouldn't happen in normal play)
        
        // Check if any opponent piece can attack the king
        String opponentColor = color.equals("white") ? "black" : "white";
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.getColor().equals(opponentColor)) {
                    // Check if this piece can move to the king's position
                    if (canAttackKing(piece, row, col, kingRow, kingCol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canAttackKing(Piece piece, int fromRow, int fromCol, int kingRow, int kingCol) {
        // For each piece type, check if it can attack the king
        switch (piece.getType()) {
            case "pawn":
                // Pawns attack diagonally
                int direction = piece.getColor().equals("white") ? -1 : 1;
                return Math.abs(fromCol - kingCol) == 1 && 
                       kingRow == fromRow + direction;
                
            case "knight":
                // Knights move in L-shape
                int rowDiff = Math.abs(fromRow - kingRow);
                int colDiff = Math.abs(fromCol - kingCol);
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
                
            case "bishop":
                // Bishops move diagonally
                if (Math.abs(fromRow - kingRow) != Math.abs(fromCol - kingCol)) {
                    return false;
                }
                return isPathClear(fromRow, fromCol, kingRow, kingCol);
                
            case "rook":
                // Rooks move straight
                if (fromRow != kingRow && fromCol != kingCol) {
                    return false;
                }
                return isPathClear(fromRow, fromCol, kingRow, kingCol);
                
            case "queen":
                // Queens move like rooks or bishops
                if (fromRow != kingRow && fromCol != kingCol && 
                    Math.abs(fromRow - kingRow) != Math.abs(fromCol - kingCol)) {
                    return false;
                }
                return isPathClear(fromRow, fromCol, kingRow, kingCol);
                
            case "king":
                // Kings can move one square in any direction
                return Math.abs(fromRow - kingRow) <= 1 && 
                       Math.abs(fromCol - kingCol) <= 1;
        }
        return false;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;
        
        while (currentRow != toRow || currentCol != toCol) {
            if (squares[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        return true;
    }
    
    public boolean isCheckmate() {
        // First check if the current player is in check
        if (!isInCheck(currentPlayer)) {
            return false;
        }
        
        // Try all possible moves for all pieces of the current player
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = squares[fromRow][fromCol];
                if (piece != null && piece.getColor().equals(currentPlayer)) {
                    // Get all possible moves for this piece
                    List<int[]> moves = getPossibleMoves(fromRow, fromCol);
                    
                    // Try each move to see if it gets out of check
                    for (int[] move : moves) {
                        int toRow = move[0];
                        int toCol = move[1];
                        
                        // Store the current state
                        Piece capturedPiece = squares[toRow][toCol];
                        int oldEnPassantTarget = enPassantTarget;
                        
                        // Make the move
                        squares[toRow][toCol] = piece;
                        squares[fromRow][fromCol] = null;
                        piece.setRow(toRow);
                        piece.setCol(toCol);
                        
                        // Check if the move gets out of check
                        boolean stillInCheck = isInCheck(currentPlayer);
                        
                        // Undo the move
                        squares[fromRow][fromCol] = piece;
                        squares[toRow][toCol] = capturedPiece;
                        piece.setRow(fromRow);
                        piece.setCol(fromCol);
                        enPassantTarget = oldEnPassantTarget;
                        
                        // If any move gets out of check, it's not checkmate
                        if (!stillInCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        
        // If we get here, the player is in check and has no moves to get out of check
        return true;
    }
    
    public boolean isStalemate() {
        if (isInCheck(currentPlayer)) return false;
        
        // Check if any move is possible
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = squares[fromRow][fromCol];
                if (piece != null && piece.getColor().equals(currentPlayer)) {
                    List<int[]> moves = getPossibleMoves(fromRow, fromCol);
                    for (int[] move : moves) {
                        int toRow = move[0];
                        int toCol = move[1];
                        
                        // Try the move
                        Piece capturedPiece = squares[toRow][toCol];
                        squares[toRow][toCol] = piece;
                        squares[fromRow][fromCol] = null;
                        piece.setRow(toRow);
                        piece.setCol(toCol);
                        
                        boolean stillInCheck = isInCheck(currentPlayer);
                        
                        // Undo the move
                        squares[fromRow][fromCol] = piece;
                        squares[toRow][toCol] = capturedPiece;
                        piece.setRow(fromRow);
                        piece.setCol(fromCol);
                        
                        if (!stillInCheck) return false;
                    }
                }
            }
        }
        return true;
    }
    
    public Piece[][] getSquares() {
        return squares;
    }
    
    public String getCurrentPlayer() {
        return currentPlayer;
    }
    
    public List<Move> getMoveHistory() {
        return moveHistory;
    }
    
    public List<int[]> getPossibleMoves(int row, int col) {
        List<int[]> possibleMoves = new ArrayList<>();
        Piece piece = squares[row][col];
        
        if (piece == null || !piece.getColor().equals(currentPlayer)) {
            return possibleMoves;
        }
        
        // If the player is in check, only allow moves that get out of check
        boolean inCheck = isInCheck(currentPlayer);
        
        switch (piece.getType().toLowerCase()) {
            case "pawn":
                addPawnMoves(row, col, piece, possibleMoves);
                break;
            case "rook":
                addStraightMoves(row, col, piece, possibleMoves);
                break;
            case "knight":
                addKnightMoves(row, col, piece, possibleMoves);
                break;
            case "bishop":
                addDiagonalMoves(row, col, piece, possibleMoves);
                break;
            case "queen":
                addStraightMoves(row, col, piece, possibleMoves);
                addDiagonalMoves(row, col, piece, possibleMoves);
                break;
            case "king":
                addKingMoves(row, col, piece, possibleMoves);
                break;
        }
        
        // Filter out moves that don't get out of check if in check
        if (inCheck) {
            List<int[]> validMoves = new ArrayList<>();
            for (int[] move : possibleMoves) {
                int toRow = move[0];
                int toCol = move[1];
                
                // Store the current state
                Piece capturedPiece = squares[toRow][toCol];
                int oldEnPassantTarget = enPassantTarget;
                
                // Make the move
                squares[toRow][toCol] = piece;
                squares[row][col] = null;
                piece.setRow(toRow);
                piece.setCol(toCol);
                
                // Check if the move gets out of check
                boolean stillInCheck = isInCheck(currentPlayer);
                
                // Undo the move
                squares[row][col] = piece;
                squares[toRow][toCol] = capturedPiece;
                piece.setRow(row);
                piece.setCol(col);
                enPassantTarget = oldEnPassantTarget;
                
                if (!stillInCheck) {
                    validMoves.add(move);
                }
            }
            return validMoves;
        }
        
        return possibleMoves;
    }
    
    private void addPawnMoves(int row, int col, Piece piece, List<int[]> moves) {
        int direction = piece.getColor().equals("white") ? -1 : 1;
        int startRow = piece.getColor().equals("white") ? 6 : 1;
        
        // Forward move
        if (isValidPosition(row + direction, col) && squares[row + direction][col] == null) {
            moves.add(new int[]{row + direction, col});
            // Double move from start position
            if (row == startRow && squares[row + 2 * direction][col] == null) {
                moves.add(new int[]{row + 2 * direction, col});
            }
        }
        
        // Capture moves
        for (int colOffset : new int[]{-1, 1}) {
            int newCol = col + colOffset;
            if (isValidPosition(row + direction, newCol) && 
                squares[row + direction][newCol] != null && 
                !squares[row + direction][newCol].getColor().equals(piece.getColor())) {
                moves.add(new int[]{row + direction, newCol});
            }
        }
    }
    
    private void addStraightMoves(int row, int col, Piece piece, List<int[]> moves) {
        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            while (isValidPosition(r, c)) {
                if (squares[r][c] == null) {
                    moves.add(new int[]{r, c});
                } else {
                    if (!squares[r][c].getColor().equals(piece.getColor())) {
                        moves.add(new int[]{r, c});
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
    }
    
    private void addDiagonalMoves(int row, int col, Piece piece, List<int[]> moves) {
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            while (isValidPosition(r, c)) {
                if (squares[r][c] == null) {
                    moves.add(new int[]{r, c});
                } else {
                    if (!squares[r][c].getColor().equals(piece.getColor())) {
                        moves.add(new int[]{r, c});
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
    }
    
    private void addKnightMoves(int row, int col, Piece piece, List<int[]> moves) {
        int[][] knightMoves = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
        for (int[] move : knightMoves) {
            int r = row + move[0];
            int c = col + move[1];
            if (isValidPosition(r, c) && (squares[r][c] == null || 
                !squares[r][c].getColor().equals(piece.getColor()))) {
                moves.add(new int[]{r, c});
            }
        }
    }
    
    private void addKingMoves(int row, int col, Piece piece, List<int[]> moves) {
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            if (isValidPosition(r, c) && (squares[r][c] == null || 
                !squares[r][c].getColor().equals(piece.getColor()))) {
                
                // Store the current state
                Piece capturedPiece = squares[r][c];
                int oldEnPassantTarget = enPassantTarget;
                
                // Make the move
                squares[r][c] = piece;
                squares[row][col] = null;
                piece.setRow(r);
                piece.setCol(c);
                
                // Check if the king would be in check at the new position
                boolean inCheck = isInCheck(piece.getColor());
                
                // Undo the move
                squares[row][col] = piece;
                squares[r][c] = capturedPiece;
                piece.setRow(row);
                piece.setCol(col);
                enPassantTarget = oldEnPassantTarget;
                
                // Only add the move if the king won't be in check
                if (!inCheck) {
                    moves.add(new int[]{r, c});
                }
            }
        }
        
        // Add castling moves if available
        if (!piece.getColor().equals("white") ? blackKingMoved : whiteKingMoved) {
            return;
        }
        
        // Check kingside castling
        if (!(piece.getColor().equals("white") ? whiteRooksMoved[1] : blackRooksMoved[1])) {
            boolean canCastle = true;
            for (int c = col + 1; c < 7; c++) {
                if (squares[row][c] != null) {
                    canCastle = false;
                    break;
                }
            }
            if (canCastle) {
                // Check if the squares the king moves through are under attack
                for (int c = col; c <= col + 2; c++) {
                    // Temporarily move the king to check if the square is under attack
                    squares[row][col] = null;
                    squares[row][c] = piece;
                    piece.setCol(c);
                    boolean isUnderAttack = isInCheck(piece.getColor());
                    squares[row][c] = null;
                    squares[row][col] = piece;
                    piece.setCol(col);
                    
                    if (isUnderAttack) {
                        canCastle = false;
                        break;
                    }
                }
                if (canCastle) {
                    moves.add(new int[]{row, col + 2});
                }
            }
        }
        
        // Check queenside castling
        if (!(piece.getColor().equals("white") ? whiteRooksMoved[0] : blackRooksMoved[0])) {
            boolean canCastle = true;
            for (int c = col - 1; c > 0; c--) {
                if (squares[row][c] != null) {
                    canCastle = false;
                    break;
                }
            }
            if (canCastle) {
                // Check if the squares the king moves through are under attack
                for (int c = col; c >= col - 2; c--) {
                    // Temporarily move the king to check if the square is under attack
                    squares[row][col] = null;
                    squares[row][c] = piece;
                    piece.setCol(c);
                    boolean isUnderAttack = isInCheck(piece.getColor());
                    squares[row][c] = null;
                    squares[row][col] = piece;
                    piece.setCol(col);
                    
                    if (isUnderAttack) {
                        canCastle = false;
                        break;
                    }
                }
                if (canCastle) {
                    moves.add(new int[]{row, col - 2});
                }
            }
        }
    }
    
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    
    public List<Piece> getCapturedByWhite() {
        return capturedByWhite;
    }

    public List<Piece> getCapturedByBlack() {
        return capturedByBlack;
    }
} 