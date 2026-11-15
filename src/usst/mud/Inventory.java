package usst.mud;

/**
 * 背包：用两个平行的一维数组保存物品与数量。
 *
 * 这里刻意不用 ArrayList，而是用定长数组 + 手工遍历，
 * 一方面便于演示"一维对象数组的管理"，另一方面把容量上限
 * （MAX_SLOTS）作为一条真正的业务规则：背包满了就捡不起来。
 */
public class Inventory {

    private static final int MAX_SLOTS = 16;

    private final Item[] items;   // 物品对象数组
    private final int[] counts;   // 与 items 下标一一对应的数量
    private int kinds;            // 已占用的格子数

    public Inventory() {
        this.items = new Item[MAX_SLOTS];
        this.counts = new int[MAX_SLOTS];
        this.kinds = 0;
    }

    /** 放入 1 件物品，成功返回 true */
    public boolean add(Item item) {
        return add(item, 1);
    }

    /** 放入 n 件同类物品：先找已有格子累加，再找空格 */
    public boolean add(Item item, int n) {
        if (item == null || n <= 0) {
            return false;
        }
        for (int i = 0; i < kinds; i++) {
            if (items[i].getId().equals(item.getId())) {
                counts[i] += n;
                return true;
            }
        }
        if (kinds >= MAX_SLOTS) {
            return false;
        }
        items[kinds] = item;
        counts[kinds] = n;
        kinds++;
        return true;
    }

    /** 按 id 统计数量（遍历数组，对应任务 3 的"查找"要求） */
    public int countOf(String id) {
        int total = 0;
        for (int i = 0; i < kinds; i++) {
            if (items[i].getId().equals(id)) {
                total += counts[i];
            }
        }
        return total;
    }

    /** 统计某一类型的物品总数，例如印章数量 */
    public int countOfType(Item.Type type) {
        int total = 0;
        for (int i = 0; i < kinds; i++) {
            if (items[i].getType() == type) {
                total += counts[i];
            }
        }
        return total;
    }

    public boolean has(String id) {
        return countOf(id) > 0;
    }

    /** 按关键字（id 或名称片段）查找物品 */
    public Item findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String key = keyword.trim().toLowerCase();
        for (int i = 0; i < kinds; i++) {
            if (items[i].getId().equalsIgnoreCase(key)
                    || items[i].getName().toLowerCase().contains(key)) {
                return items[i];
            }
        }
        return null;
    }

    /** 移除 n 件物品，成功返回 true */
    public boolean remove(String id, int n) {
        if (n <= 0) {
            return false;
        }
        for (int i = 0; i < kinds; i++) {
            if (!items[i].getId().equals(id)) {
                continue;
            }
            if (counts[i] < n) {
                return false;
            }
            counts[i] -= n;
            if (counts[i] == 0) {
                // 把最后一格挪到当前位置，保持数组连续
                for (int j = i; j < kinds - 1; j++) {
                    items[j] = items[j + 1];
                    counts[j] = counts[j + 1];
                }
                kinds--;
                items[kinds] = null;
                counts[kinds] = 0;
            }
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return kinds == 0;
    }

    /** 背包是否已满（用于任务奖励前的容量检查） */
    public boolean isFull() {
        return kinds >= MAX_SLOTS;
    }

    /** 供界面显示的一行文字，例如 "肉包×1、猫粮×2" */
    public String list() {
        if (kinds == 0) {
            return "（空）";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kinds; i++) {
            if (i > 0) {
                sb.append("、");
            }
            sb.append(items[i].getName());
            if (counts[i] > 1) {
                sb.append("×").append(counts[i]);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return list();
    }
}
