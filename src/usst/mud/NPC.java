package usst.mud;

import java.util.Random;

/**
 * 校园 NPC。
 *
 * talk() 是一个带业务规则的对象方法：它自己判断"玩家是否答够题、
 * 是否带着任务道具"，满足条件才发放学分和印章，并且只发放一次。
 * 这样 Game 里就不需要写一堆 if，职责边界很清晰。
 */
public class NPC {

    private final String name;
    private final String role;
    private final int roomId;
    private final String[] lines;        // 闲聊台词，随机抽一句
    private final String requireItemId;  // 需要交给他的物品 id，可为 null
    private final String rewardItemId;   // 交任务后给的物品 id，可为 null
    private final int creditReward;
    private final int needSolved;        // 需要已答对的题目数
    private final String hint;           // 条件不满足时的提示
    private final Random random = new Random();
    private boolean finished;

    public NPC(String name, String role, int roomId, String[] lines,
               String requireItemId, String rewardItemId, int creditReward,
               int needSolved, String hint) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("NPC 名字不能为空");
        }
        if (roomId < 0) {
            throw new IllegalArgumentException("NPC 所在场景不合法");
        }
        if (creditReward < 0 || creditReward > Player.MAX_CREDIT) {
            throw new IllegalArgumentException("NPC 奖励学分不合法");
        }
        this.name = name.trim();
        this.role = role == null ? "" : role;
        this.roomId = roomId;
        this.lines = lines == null ? new String[]{"……"} : lines.clone();
        this.requireItemId = requireItemId;
        this.rewardItemId = rewardItemId;
        this.creditReward = creditReward;
        this.needSolved = needSolved;
        this.hint = hint == null ? "……" : hint;
    }

    /**
     * 与 NPC 对话。
     * 返回值直接就是给玩家看的文本，NPC 内部完成"校验 -> 扣道具 -> 发奖励"。
     */
    public String talk(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("玩家不能为空");
        }
        // 1. 已经完成过的 NPC，只说闲话
        if (finished) {
            return name + "：" + randomLine();
        }
        // 2. 题目数量不够
        if (needSolved > 0 && player.getSolvedCount() < needSolved) {
            return name + "：" + hint + "\n（进度：已答对 " + player.getSolvedCount()
                    + "/" + needSolved + " 题）";
        }
        // 3. 缺少任务道具
        if (requireItemId != null && !player.getBag().has(requireItemId)) {
            return name + "：" + hint;
        }
        // 4. 条件满足：扣除道具、发放奖励
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("：").append(randomLine());
        if (requireItemId != null) {
            Item required = CampusData.itemById(requireItemId);
            player.getBag().remove(requireItemId, 1);
            sb.append("\n（你递上了「").append(required == null ? requireItemId : required.getName())
                    .append("」）");
        }
        if (creditReward > 0) {
            sb.append("\n").append(player.addCredit(creditReward));
        }
        if (rewardItemId != null) {
            Item reward = CampusData.itemById(rewardItemId);
            if (reward != null && player.getBag().add(reward)) {
                sb.append("\n你获得了「").append(reward.getName()).append("」。");
            }
        }
        finished = true;
        return sb.toString();
    }

    private String randomLine() {
        return lines[random.nextInt(lines.length)];
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public int getRoomId() {
        return roomId;
    }

    public boolean isFinished() {
        return finished;
    }
}
