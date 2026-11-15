package usst.mud;

/**
 * 校园"对手"：由学业压力具象化而来的怪物。
 * 体力（HP）与攻击力都是 private，只能通过 takeDamage / reset 修改。
 */
public class Monster {

    private final String name;
    private final int roomId;
    private final int maxHp;
    private final int attack;
    private final int defend;
    private final int creditReward;
    private final String dropItemId;
    private final boolean boss;
    private final int needStamps;
    private final String intro;

    private int hp;
    private boolean defeated;

    public Monster(String name, int roomId, int maxHp, int attack, int defend,
                   int creditReward, String dropItemId, boolean boss,
                   int needStamps, String intro) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("对手名称不能为空");
        }
        if (maxHp <= 0) {
            throw new IllegalArgumentException("对手血量必须大于 0：" + name);
        }
        if (attack <= 0 || defend < 0) {
            throw new IllegalArgumentException("对手攻击/防御数值不合法：" + name);
        }
        this.name = name.trim();
        this.roomId = roomId;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defend = defend;
        this.creditReward = creditReward;
        this.dropItemId = dropItemId;
        this.boss = boss;
        this.needStamps = needStamps;
        this.intro = intro == null ? "" : intro;
        this.hp = maxHp;
        this.defeated = false;
    }

    public String getName() {
        return name;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefend() {
        return defend;
    }

    public int getCreditReward() {
        return creditReward;
    }

    public String getDropItemId() {
        return dropItemId;
    }

    public boolean isBoss() {
        return boss;
    }

    public int getNeedStamps() {
        return needStamps;
    }

    public String getIntro() {
        return intro;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    /** 扣血，返回实际造成的伤害（不会把血量扣成负数） */
    public int takeDamage(int damage) {
        if (damage <= 0) {
            return 0;
        }
        int before = hp;
        hp = Math.max(0, hp - damage);
        return before - hp;
    }

    /** 战斗结束时把血量复原，方便玩家休息后再来挑战 */
    public void reset() {
        this.hp = maxHp;
    }

    @Override
    public String toString() {
        return name + "（" + hp + "/" + maxHp + "）";
    }
}
