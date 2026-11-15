package usst.mud;

/**
 * 校园物品。
 *
 * 设计要点（对应任务 2 的封装要求）：
 * 1. 所有字段都是 private final，对象一旦创建就不能被外部随意改写；
 * 2. 构造方法里做合法性校验，物品 id 为空、体力恢复为负数都会直接抛异常；
 * 3. 物品只描述自己，不负责"被使用"的流程，使用规则放在 Player.useItem 里。
 */
public class Item {

    /** 物品类型：食物、工具、任务道具、印章 */
    public enum Type {
        FOOD, TOOL, QUEST, STAMP
    }

    private final String id;
    private final String name;
    private final String description;
    private final Type type;
    private final int energyRestore;   // 使用后恢复的体力
    private final int creditBonus;     // 使用后增加的学分

    public Item(String id, String name, String description, Type type,
                int energyRestore, int creditBonus) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("物品 id 不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("物品名称不能为空：" + id);
        }
        if (energyRestore < 0 || energyRestore > Player.MAX_ENERGY) {
            throw new IllegalArgumentException("恢复体力必须在 0~" + Player.MAX_ENERGY + " 之间：" + id);
        }
        if (creditBonus < 0 || creditBonus > Player.MAX_CREDIT) {
            throw new IllegalArgumentException("物品学分加成必须在 0~" + Player.MAX_CREDIT + " 之间：" + id);
        }
        this.id = id.trim();
        this.name = name.trim();
        this.description = description == null ? "" : description;
        this.type = type;
        this.energyRestore = energyRestore;
        this.creditBonus = creditBonus;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public int getEnergyRestore() {
        return energyRestore;
    }

    public int getCreditBonus() {
        return creditBonus;
    }

    /** 可以直接吃/喝的东西 */
    public boolean isConsumable() {
        return type == Type.FOOD;
    }

    @Override
    public String toString() {
        return name + "（" + description + "）";
    }
}
