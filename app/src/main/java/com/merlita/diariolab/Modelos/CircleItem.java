package com.merlita.diariolab.Modelos;

public class CircleItem {
    private int diameter; // Diámetro en dp (0-100)
    private int gridX;    // Posición X en la grid
    private int gridY;    // Posición Y en la grid

    public CircleItem(int diameter, int gridX, int gridY) {
        this.diameter = Math.max(10, Math.min(diameter, 100)); // Mínimo 10dp, máximo 100dp
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public CircleItem(int diameter) {
        this.diameter = Math.max(10, Math.min(diameter, 100)); // Mínimo 10dp, máximo 100dp
    }

    // Getters
    public int getDiameter() { return diameter; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
}