package usst.mud;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家角色。
 *
 * 封装的三个体现（任务 2）：
 * 1. 所有会破坏平衡的字段（energy、credit、actionsLeft、currentRoom）都是 private；
 * 2. 外部不能直接 energy -= 20，只能调用 consumeEnergy / restoreEnergy；
 * 3. 体力归零时，consumeEnergy 内部会自动触发"强制回宿舍 + 扣行动次数"的联动，
 *    调用方根本不需要知道这个规则。
 */
public class Player {

    public static final int MAX_ENERGY = 100;      // 体力上限
    public static final int MAX_CREDIT = 100;      // 学分上限
    public static final int MOVE_COST = 5;         // 每次移动消耗体力
    public static final int DAILY_ACTIONS = 30;    // 每天的行动次数

    private static final int COLLAPSE_ACTION_PENALTY = 5;  // 体力透支扣除的行动次数
    private static final int COLLAPSE_ENERGY = 20;         // 醒来后的体力

    private final String name;
    private final Room respawnRoom;      // 体力透支后强制传送的场景（宿舍）
    private final Inventory bag;
    private final Map<String, Integer> questStages;   // 支线 id -> 当前阶段

    private int energy;
    private int credit;
    private int actionsLeft;
    private Room currentRoom;
    private int solvedCount;
    private int killCount;
    private boolean justCollapsed;

    public Player(String name, Room startRoom, Room respawnRoom) {
        if (name == null || name.replace("\uFEFF", "").trim().isEmpty()) {
            throw new IllegalArgumentException("玩家名字不能为空");
        }
        // 去掉从记事本/文件里粘进来的 BOM 字符，避免名字前面出现看不见的方块
        String trimmed = name.replace("\uFEFF", "").trim();
        if (trimmed.length() > 12) {
            throw new IllegalArgumentException("玩家名字不能超过 12 个字符");
        }
        if (startRoom == null || respawnRoom == null) {
            throw new IllegalArgumentException("出生场景和复活场景都不能为空");
        }
        this.name = trimmed;
        this.currentRoom = startRoom;
        this.respawnRoom = respawnRoom;
        this.bag = new Inventory();
        this.questStages = new HashMap<>();
        this.energy = MAX_ENERGY;
        this.credit = 0;
        this.actionsLeft = DAILY_ACTIONS;
        this.solvedCount = 0;
        this.killCount = 0;
        this.justCollapsed = false;
    }

    // ==================== 体力相关的业务方法 ====================

    /**
     * 消耗体力。
     * 规则：体力不能小于 0；一旦减到 0，立即触发强制状态变更：
     *   1) 位置重置回宿舍；
     *   2) 当日行动次数 -5；
     *   3) 体力恢复到 20，避免玩家卡在"永远 0 体力"。
     *
     * @param amount 消耗量，必须 >= 0
     * @return 给玩家看的反馈文字
     */
    public String consumeEnergy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("体力消耗值不能为负数：" + amount);
        }
        justCollapsed = false;
        if (amount == 0) {
            return "";
        }
        int before = energy;
        energy = Math.max(0, energy - amount);
        StringBuilder sb = new StringBuilder();
        sb.append("体力 -").append(before - energy).append(" → ").append(energy).append("/").append(MAX_ENERGY);
        if (energy == 0) {
            sb.append("\n").append(collapse());
        }
        return sb.toString();
    }

    /**
     * 体力透支的强制状态变更。
     * 注意：这个方法本身是 private，外部无法"手动晕倒"，
     * 只能在 consumeEnergy 检测到 energy == 0 时被调用。
     */
    private String collapse() {
        justCollapsed = true;
        energy = COLLAPSE_ENERGY;
        actionsLeft = Math.max(0, actionsLeft - COLLAPSE_ACTION_PENALTY);
        currentRoom = respawnRoom;
        StringBuilder sb = new StringBuilder();
        sb.append("【体力透支】你两眼一黑，再睁眼已经躺在「").append(respawnRoom.getName()).append("」的床上。");
        sb.append("\n→ 强制状态变更：位置重置为「").append(respawnRoom.getName()).append("」");
        sb.append("；当日行动次数 -").append(COLLAPSE_ACTION_PENALTY)
                .append("（剩余 ").append(actionsLeft).append(" 次）");
        sb.append("；体力恢复到 ").append(COLLAPSE_ENERGY).append("。");
        return sb.toString();
    }

    /** 恢复体力，上限 100（Math.min 负责封顶） */
    public String restoreEnergy(int amount) {
        if (amount <= 0) {
            return "";
        }
        int before = energy;
        energy = Math.min(MAX_ENERGY, energy + amount);
        return "体力 +" + (energy - before) + " → " + energy + "/" + MAX_ENERGY;
    }

    // ==================== 学分相关的业务方法 ====================

    /**
     * 增加/扣减学分。
     * 规则：学分严格锁定在 0~100，用 Math.max / Math.min 把越界值夹回来。
     */
    public String addCredit(int delta) {
        int before = credit;
        credit = Math.max(0, Math.min(MAX_CREDIT, credit + delta));
        int real = credit - before;
        StringBuilder sb = new StringBuilder();
        sb.append("学分 ").append(real >= 0 ? "+" : "").append(real)
                .append(" → ").append(credit).append("/").append(MAX_CREDIT);
        if (delta > 0 && before + delta > MAX_CREDIT) {
            sb.append("（已达上限，超出部分不计入）");
        }
        if (delta < 0 && before + delta < 0) {
            sb.append("（最低为 0，不会变成负分）");
        }
        return sb.toString();
    }

    // ==================== 移动 ====================

    /**
     * 移动到目标场景：先扣体力，再改位置。
     * 如果这一步正好把体力走空，consumeEnergy 会把玩家送回宿舍，
     * 此时不能再把位置改成目标场景——这正是封装在这里替外部挡住的坑。
     */
    public String moveTo(Room target) {
        if (target == null) {
            throw new IllegalArgumentException("目标场景不能为空");
        }
        if (target == currentRoom) {
            return "你已经在「" + target.getName() + "」了。";
        }
        String energyText = consumeEnergy(MOVE_COST);
        if (justCollapsed) {
            return energyText;
        }
        currentRoom = target;
        return "你来到「" + target.getName() + "」。（" + energyText + "）";
    }

    // ==================== 物品 ====================

    /**
     * 使用物品：从背包移除 -> 生效 -> 返回文字。
     * 印章和任务道具不允许"使用"，由这里统一拦截。
     */
    public String useItem(Item item) {
        if (item == null) {
            return "没有这件物品。";
        }
        if (item.getType() == Item.Type.STAMP) {
            return "「" + item.getName() + "」是印章，不能吃也不能用，收好它。";
        }
        if (item.getType() == Item.Type.QUEST) {
            return "「" + item.getName() + "」是任务道具，交给需要它的人吧。";
        }
        if (!bag.remove(item.getId(), 1)) {
            return "背包里没有「" + item.getName() + "」。";
        }
        StringBuilder sb = new StringBuilder("你使用了「").append(item.getName()).append("」。");
        if (item.getEnergyRestore() > 0) {
            sb.append("\n").append(restoreEnergy(item.getEnergyRestore()));
        }
        if (item.getCreditBonus() > 0) {
            sb.append("\n").append(addCredit(item.getCreditBonus()));
        }
        return sb.toString();
    }

    // ==================== 时间 / 行动 ====================

    /** 消耗 1 次行动次数，成功返回 true */
    public boolean spendAction() {
        if (actionsLeft <= 0) {
            return false;
        }
        actionsLeft--;
        return true;
    }

    /** 新的一天：行动次数刷新、体力回满 */
    public String startNewDay() {
        actionsLeft = DAILY_ACTIONS;
        energy = MAX_ENERGY;
        return "睡了 7 个小时，体力回满 100，行动次数刷新为 " + DAILY_ACTIONS + " 次。";
    }

    // ==================== 查询方法 ====================

    public String getName() {
        return name;
    }

    public int getEnergy() {
        return energy;
    }

    public int getCredit() {
        return credit;
    }

    public int getActionsLeft() {
        return actionsLeft;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public Inventory getBag() {
        return bag;
    }

    public Room getRespawnRoom() {
        return respawnRoom;
    }

    public int getSolvedCount() {
        return solvedCount;
    }

    public void addSolvedCount() {
        solvedCount++;
    }

    public int getKillCount() {
        return killCount;
    }

    public void addKillCount() {
        killCount++;
    }

    public int getStampCount() {
        return bag.countOfType(Item.Type.STAMP);
    }

    /** 读取某条支线的阶段，未开始时为 0 */
    public int getQuestStage(String questId) {
        Integer stage = questStages.get(questId);
        return stage == null ? 0 : stage;
    }

    /** 推进/回退某条支线的阶段，负数会被夹到 0 */
    public void setQuestStage(String questId, int stage) {
        if (questId == null || questId.trim().isEmpty()) {
            throw new IllegalArgumentException("支线 id 不能为空");
        }
        questStages.put(questId, Math.max(0, stage));
    }

    /** 刚刚这一次操作是否触发了体力透支（由 consumeEnergy 复位） */
    public boolean hasJustCollapsed() {
        return justCollapsed;
    }

    /** 等级由学分推导，不单独存储，避免"学分和等级不一致"的脏数据 */
    public String getLevel() {
        if (credit >= 90) {
            return "上理传奇";
        }
        if (credit >= 70) {
            return "学霸";
        }
        if (credit >= 50) {
            return "老上理";
        }
        if (credit >= 25) {
            return "适应期新生";
        }
        return "萌新";
    }

    /** 任务 5 的 Object 要求：用 toString 输出完整状态面板 */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----------------------------------------------\n");
        sb.append(name).append(" · ").append(getLevel()).append("\n");
        sb.append("学分 ").append(credit).append("/").append(MAX_CREDIT)
                .append("    体力 ").append(energy).append("/").append(MAX_ENERGY).append("\n");
        sb.append("行动 ").append(actionsLeft).append("/").append(DAILY_ACTIONS)
                .append("    位置 ").append(currentRoom.getName()).append("\n");
        sb.append("已解谜题 ").append(solvedCount)
                .append("    击败对手 ").append(killCount)
                .append("    印章 ").append(getStampCount()).append("/").append(CampusData.TOTAL_STAMPS).append("\n");
        sb.append("背包：").append(bag.list()).append("\n");
        sb.append("----------------------------------------------");
        return sb.toString();
    }
}
