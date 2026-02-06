package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player extends GameEntity {
    private int health;
    private int maxHealth;
    private int score;
    private int shield;
    private int potionCount;
    private int trapCount;

    public Player(Position position) {
        super(position, Tileset.AVATAR, "Player");
        this.maxHealth = 10;
        this.health = 10;
        this.score = 0;
        this.shield = 0;
        this.potionCount = 1;
        this.trapCount = 3;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int amount) {
        if (shield > 0) {
            int damageToShield = Math.min(shield, amount);
            shield -= damageToShield;
            amount -= damageToShield;
        }
        
        if (amount > 0) {
            this.health -= amount;
            if (this.health < 0) this.health = 0;
        }
    }

    public void usePotion() {
        if (potionCount > 0 && health < maxHealth) {
            potionCount--;
            health = Math.min(health + 2, maxHealth);
        }
    }

    public boolean placeTrap() {
        if (trapCount > 0) {
            trapCount--;
            return true;
        }
        return false;
    }

    public void addScore(int amount) {
        this.score += amount;
    }
    
    public int getScore() {
        return score;
    }

    public void addShield(int amount) {
        this.shield += amount;
    }

    public void addPotion(int amount) {
        this.potionCount += amount;
    }

    public void addTrap(int amount) {
        this.trapCount += amount;
    }

    public int getShield() {
         return shield;
    }

    public int getPotionCount() {
        return potionCount;
    }

    public int getTrapCount() {
        return trapCount;
    }
}
