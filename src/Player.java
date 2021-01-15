public class Player {

    // TODO: Move to private, then getter methods to change health.
    private String playerName;
    public int playerMaxHealth;
    public int playerCurrentHealth;
    public int playerAttackPower;
    public int playerDefense;

    public Player(String playerName, int playerHealth){
        this.playerName = playerName;
        this.playerMaxHealth = playerHealth;

        // Initially, playerCurrentHealth == playerMaxHealth
        playerCurrentHealth = playerMaxHealth;
    }

    public String getPlayerName(){
        return playerName;
    }

    public int getPlayerHealth(){
        return playerCurrentHealth;
    }

    public int updatePlayerHealth(int _damage){
        int updatedHP = getPlayerHealth() - _damage;
        return updatedHP;

    }
}
