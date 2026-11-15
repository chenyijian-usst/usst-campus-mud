package usst.mud;

/**
 * 随机校园事件。
 *
 * 事件本身只保存"效果数据"，真正的数值修改交给 Player 的
 * consumeEnergy / restoreEnergy / addCredit 方法去做，
 * 这样"体力不能小于 0、学分不能超过 100"的规则永远只有一处实现。
 */
public class CampusEvent {

    private final String title;
    private final String text;
    private final int energyDelta;   // 正数恢复，负数消耗
    private final int creditDelta;
    private final String itemId;     // 事件附赠的物品，可为 null

    public CampusEvent(String title, String text, int energyDelta, int creditDelta, String itemId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("事件标题不能为空");
        }
        this.title = title.trim();
        this.text = text == null ? "" : text;
        this.energyDelta = energyDelta;
        this.creditDelta = creditDelta;
        this.itemId = itemId;
    }

    /** 把事件效果作用到玩家身上，返回要显示的文字 */
    public String apply(Player player) {
        StringBuilder sb = new StringBuilder();
        if (energyDelta > 0) {
            sb.append(player.restoreEnergy(energyDelta));
        } else if (energyDelta < 0) {
            sb.append(player.consumeEnergy(-energyDelta));
        }
        if (creditDelta != 0) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(player.addCredit(creditDelta));
        }
        if (itemId != null) {
            Item item = CampusData.itemById(itemId);
            if (item != null && player.getBag().add(item)) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("获得物品：").append(item.getName());
            }
        }
        return sb.toString();
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return title + "：" + text;
    }
}
