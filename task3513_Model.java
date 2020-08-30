package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    int score, maxTile;
    Stack previousStates = new Stack();
    Stack previousScores = new Stack();
    boolean isSaveNeeded = true;

    public Model() {
        score = 0;
        maxTile = 0;
        resetGameTiles();
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> list = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                Tile tile = this.gameTiles[i][j];
                if (tile.isEmpty()) list.add(tile);
            }
        }
        return list;
    }

    public void left() {
        if (isSaveNeeded){
            saveState(gameTiles);
        }
        boolean isChanged = false;
        for (Tile[] row : gameTiles) {
            if (compressTiles(row)) {
                isChanged = true;
            }
            if (mergeTiles(row)) {
                isChanged = true;
            }
        }
        if (isChanged) addTile();
        isSaveNeeded= true;

    }

    public void right(){
        saveState(gameTiles);
        rotateClockwise();
        rotateClockwise();
        left();
        rotateClockwise();
        rotateClockwise();

    }

    public void up(){
        saveState(gameTiles);
        for (int i = 0; i < 3; i++) {
            rotateClockwise();
        }
        left();
        rotateClockwise();
    }

    public void down(){
        saveState(gameTiles);
        rotateClockwise();
        left();
        for (int i = 0; i < 3; i++) {
            rotateClockwise();
        }
    }

    private void rotateClockwise() {
        Tile[][] tmpMatrix = new Tile[gameTiles.length][gameTiles.length];

        for (int row = 0; row < gameTiles.length; row++) {
            for (int col = 0; col < gameTiles[0].length; col++) {
                tmpMatrix[col][row] = gameTiles[gameTiles.length - row - 1][col];
            }
        }

        for (int row = 0; row < gameTiles.length; row++) {
            for (int col = 0; col < gameTiles[0].length; col++) {
                gameTiles[row][col] = tmpMatrix[row][col];
            }
        }
    }


    private boolean mergeTiles(Tile[] tiles){

        boolean arrayBeChanged = false;

        // 0. Начиная со второй плитки
        for (int i = 1; i < tiles.length; i++) {

            // 1.
            Tile left = tiles[i - 1];
            Tile right = tiles[i];

            // 2.
            if (left.isEmpty() || right.isEmpty()) continue;
            if (left.value != right.value) continue;

            // 3.
            left.value = left.value + right.value;
            right.value = 0;

            // 4.
            score = score + left.value;
            maxTile = Math.max(maxTile, left.value);
            arrayBeChanged = true;
        }

        // 5.
        arrayBeChanged = arrayBeChanged || compressTiles(tiles);
        compressTiles(tiles);
        return arrayBeChanged;

    }
    private boolean compressTiles(Tile[] tiles){
        boolean isChanged = true;
        boolean arrayBeChanged = false;
        while (isChanged){
            isChanged = false;
            for (int i = tiles.length - 1; i > 0; i--) {
                Tile curr = tiles[i];
                if (curr.isEmpty()) continue;
                if (tiles[i - 1].isEmpty()) {
                    Tile swap = tiles[i-1];
                    tiles[i-1] = tiles[i];
                    tiles[i] = swap;
                    isChanged = true;
                    arrayBeChanged = true;
                    break;
                }
            }

        }
        return arrayBeChanged;

    }

    protected void resetGameTiles(){
        this.gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                this.gameTiles[i][j] = new Tile();
            }
        }

        addTile();
        addTile();

    }

    private void addTile() {
        List<Tile> list = getEmptyTiles();
        if (list.size() == 0) return;
        Tile tile = list.get((int) (Math.random() * list.size()));
        tile.value = (Math.random() < 0.9 ? 2 : 4);
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove(){
        for(int i = 0; i < gameTiles.length; i++) {
            if(compressTiles(gameTiles[i])) return true;
            if(mergeTiles(gameTiles[i])) return true;

        }

        Tile[] tiles = new Tile[gameTiles.length];
        for(int i = 0; i < gameTiles[0].length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                if(gameTiles[j][i].isEmpty())return true;
                tiles[j] = gameTiles[j][i];
            }
            if(compressTiles(tiles)) return true;
            if(mergeTiles(tiles)) return true;

        }
        return false;
    }

    private void saveState(Tile[][] tiles){
        Tile[][] tile2 = new Tile[tiles.length][tiles.length];
        for (int i=0; i<tiles.length;i++){
            for (int j=0;j<tiles.length;j++){
            tile2[i][j] =new Tile(tiles[i][j].value);}
        }
        previousStates.push(tile2);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if (!previousScores.empty()&&!previousStates.empty()){
        gameTiles=(Tile[][]) previousStates.pop();
        score=(int)previousScores.pop();}
    }

    public void randomMove(){
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n){
            case 0: left();
            break;
            case 1: right();
            break;
            case 2: up();
            break;
            case 3: down();
        }
    }
    public boolean hasBoardChanged(){
        Tile[][] lastBoard = (Tile[][]) previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (lastBoard[i][j].value != gameTiles[i][j].value) {
                    return true;
                }
            }
        }

        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move){
        move.move();
        if (!hasBoardChanged()) {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }

        int emptyTilesCount = getEmptyTiles().size();

        MoveEfficiency moveEfficiency = new MoveEfficiency(emptyTilesCount, score, move);
        rollback();

        return moveEfficiency;
    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue(FIELD_WIDTH, Collections.reverseOrder());
        queue.add(getMoveEfficiency(() -> left()));
        queue.add(getMoveEfficiency(() -> up()));
        queue.add(getMoveEfficiency(() -> right()));
        queue.add(getMoveEfficiency(() -> down()));
        queue.peek().getMove().move();
    }
}
