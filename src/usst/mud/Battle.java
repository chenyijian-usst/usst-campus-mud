package usst.mud;

import java.util.Random;

/**
 * 回合制对决：把"学业压力"打成一场你来我往的辩论。
 *
 * 设计要点：
 * 1. 战斗不直接操作玩家的 energy 字段，一律走 consumeEnergy，
 *    所以体力归零时 Player 内部的"强制回宿舍"联动会自动生效；
 * 2. 每一回合的伤害由 Random 决定，暴击、逃跑成功率也由 Random 决定；
 * 3. 战斗状态（over / won / escaped）由 Battle 自己维护，Game 只负责读结果。
 */
public class Battle {

    private final Player player;
    private final Monster monster;
    private final Random random;

    private int round;
    private boolean over;
    private boolean won;
    private boolean escaped;
    private boolean defending;

    public Battle(Player player, Monster monster) {
        this(player, monster, new Random());
    }

    /** 允许外部注入随机源，配合 --seed 参数复现同一场战斗 */
    public Battle(Player player, Monster monster, Random random) {
        if (player == null || monster == null) {
            throw new IllegalArgumentException("战斗双方都不能为空");
        }
        this.player = player;
        this.monster = monster;
        this.random = random == null ? new Random() : random;
        this.round = 0;
        this.over = false;
        this.won = false;
        this.escaped = false;
        this.defending = false;
        monster.reset();
    }

    public String start() {
        StringBuilder sb = new StringBuilder();
        sb.append("⚔ 「").append(monster.getName()).append("」拦住了你的去路！");
        if (!monster.getIntro().isEmpty()) {
            sb.append("\n").append(monster.getIntro());
        }
        sb.append("\n它还有 ").append(monster.getHp()).append(" 点气势，你的体力是 ")
                .append(player.getEnergy()).append("/100。");
        sb.append("\n可用指令：attack（进攻）  defend（防守）  flee（逃跑）  use 物品");
        return sb.toString();
    }

    /** 玩家进攻 */
    public String playerAttack() {
        if (over) {
            return "战斗已经结束。";
        }
        round++;
        // 基础伤害 6~12，学分越高越会"讲道理"，最多加 10 点
        int base = 6 + random.nextInt(7) + player.getCredit() / 10;
        boolean critical = random.nextInt(100) < 12;
        int damage = Math.max(1, base * (critical ? 2 : 1) - monster.getDefend());
        monster.takeDamage(damage);

        StringBuilder sb = new StringBuilder();
        sb.append("第 ").append(round).append(" 回合：你抓住对方话里的漏洞，发动「课堂抢答」");
        if (critical) {
            sb.append("，暴击！");
        }
        sb.append("，对「").append(monster.getName()).append("」造成 ").append(damage).append(" 点伤害");
        sb.append("（它还剩 ").append(monster.getHp()).append("/").append(monster.getMaxHp()).append("）");

        if (!monster.isAlive()) {
            over = true;
            won = true;
            sb.append("\n").append(settleVictory());
            return sb.toString();
        }
        sb.append("\n").append(monsterTurn());
        return sb.toString();
    }

    /** 玩家防守：本回合受到的伤害减半 */
    public String playerDefend() {
        if (over) {
            return "战斗已经结束。";
        }
        round++;
        defending = true;
        StringBuilder sb = new StringBuilder();
        sb.append("第 ").append(round).append(" 回合：你抱紧书包摆出防守姿态（本回合伤害减半）。");
        sb.append("\n").append(monsterTurn());
        return sb.toString();
    }

    /** 玩家逃跑：55% 成功 */
    public String playerFlee() {
        if (over) {
            return "战斗已经结束。";
        }
        round++;
        if (random.nextInt(100) < 55) {
            over = true;
            won = false;
            escaped = true;
            monster.reset();
            return "你趁对方整理措辞的空档溜走了……（战斗结束，没有奖励）";
        }
        return "逃跑失败！「" + monster.getName() + "」挡住了你的退路。\n" + monsterTurn();
    }

    /** 玩家在战斗中使用了物品，之后对手行动一次 */
    public String playerUsedItem() {
        if (over) {
            return "";
        }
        round++;
        return monsterTurn();
    }

    /** 对手回合：造成 attack ~ attack+3 点伤害，防守状态下减半 */
    public String monsterTurn() {
        int damage = monster.getAttack() + random.nextInt(4);
        if (defending) {
            damage = damage / 2;
            defending = false;
        }
        damage = Math.max(1, damage);
        String energyText = player.consumeEnergy(damage);

        StringBuilder sb = new StringBuilder();
        sb.append("「").append(monster.getName()).append("」反击，你被怼得说不出话。");
        sb.append(energyText);

        if (player.hasJustCollapsed()) {
            over = true;
            won = false;
            monster.reset();
            sb.append("\n你眼前一黑，倒在了「").append(monster.getName()).append("」面前……");
        }
        return sb.toString();
    }

    /** 胜利结算：加学分、掉物品 */
    private String settleVictory() {
        StringBuilder sb = new StringBuilder();
        sb.append("✦ 你赢了！「").append(monster.getName()).append("」化作一页被撕碎的笔记消失了。");
        sb.append("\n").append(player.addCredit(monster.getCreditReward()));
        if (monster.getDropItemId() != null) {
            Item item = CampusData.itemById(monster.getDropItemId());
            if (item != null) {
                if (player.getBag().add(item)) {
                    sb.append("\n战利品：").append(item.getName());
                } else {
                    sb.append("\n（背包已满，").append(item.getName()).append("没能捡起来）");
                }
            }
        }
        return sb.toString();
    }

    public boolean isOver() {
        return over;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isEscaped() {
        return escaped;
    }

    public Monster getMonster() {
        return monster;
    }

    public int getRound() {
        return round;
    }
}
