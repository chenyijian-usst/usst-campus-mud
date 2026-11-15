package usst.mud;

/**
 * 校园场景（Room）。
 *
 * 出口用 int[6] 保存"目标场景在 CampusData.ROOMS 数组中的下标"，
 * -1 表示这个方向没有路。用下标而不是对象引用的好处是：
 * 地图数据可以被打印、被遍历，也可以显式地检查"下标越界"。
 */
public class Room {

    /** 六个方向，下标即出口数组的槽位 */
    public static final String[] DIRECTIONS = {"north", "south", "east", "west", "up", "down"};
    public static final String[] DIRECTION_CN = {"北", "南", "东", "西", "上", "下"};

    private final int id;
    private final String name;
    private final String description;
    private final String feature;
    private final int[] exits;              // 长度 6，-1 表示无出口
    private final String[] lootIds;         // 可搜到的随机物品 id
    private final String guaranteedItemId;  // 第一次搜索必定获得的物品 id
    private final boolean canRest;          // 是否可以休息恢复体力
    private boolean guaranteedTaken;

    public Room(int id, String name, String description, String feature,
                String[] lootIds, String guaranteedItemId, boolean canRest) {
        if (id < 0) {
            throw new IllegalArgumentException("场景 id 不能为负数");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("场景名称不能为空");
        }
        this.id = id;
        this.name = name.trim();
        this.description = description == null ? "" : description;
        this.feature = feature == null ? "" : feature;
        this.exits = new int[DIRECTIONS.length];
        for (int i = 0; i < exits.length; i++) {
            exits[i] = -1;   // 默认四通八达地"没有路"
        }
        this.lootIds = lootIds == null ? new String[0] : lootIds;
        this.guaranteedItemId = guaranteedItemId;
        this.canRest = canRest;
        this.guaranteedTaken = false;
    }

    /**
     * 把方向字符串翻译成出口数组下标。
     * 支持 north/n/北、south/s/南 等写法，非法方向返回 -1。
     */
    public static int directionIndex(String dir) {
        if (dir == null) {
            return -1;
        }
        String d = dir.trim();
        if (d.equalsIgnoreCase("north") || d.equalsIgnoreCase("n") || d.equals("北") || d.equals("前")) {
            return 0;
        }
        if (d.equalsIgnoreCase("south") || d.equalsIgnoreCase("s") || d.equals("南") || d.equals("后")) {
            return 1;
        }
        if (d.equalsIgnoreCase("east") || d.equalsIgnoreCase("e") || d.equals("东") || d.equals("右")) {
            return 2;
        }
        if (d.equalsIgnoreCase("west") || d.equalsIgnoreCase("w") || d.equals("西") || d.equals("左")) {
            return 3;
        }
        if (d.equalsIgnoreCase("up") || d.equalsIgnoreCase("u") || d.equals("上")) {
            return 4;
        }
        if (d.equalsIgnoreCase("down") || d.equalsIgnoreCase("d") || d.equals("下")) {
            return 5;
        }
        return -1;
    }

    /** 设置出口：dir 是方向名，targetId 是目标场景在 ROOMS 中的下标 */
    public void setExit(String dir, int targetId) {
        int idx = directionIndex(dir);
        if (idx < 0) {
            throw new IllegalArgumentException("未知方向：" + dir);
        }
        if (targetId < 0) {
            throw new IllegalArgumentException("目标场景下标不能为负数：" + targetId);
        }
        exits[idx] = targetId;
    }

    /**
     * 取某个方向的出口下标。
     * 这里对下标本身再做一次校验，防止外部传入非法下标导致数组越界。
     */
    public int getExit(int directionIndex) {
        if (directionIndex < 0 || directionIndex >= exits.length) {
            return -1;
        }
        return exits[directionIndex];
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFeature() {
        return feature;
    }

    public String[] getLootIds() {
        return lootIds;
    }

    public String getGuaranteedItemId() {
        return guaranteedItemId;
    }

    public boolean isCanRest() {
        return canRest;
    }

    public boolean isGuaranteedTaken() {
        return guaranteedTaken;
    }

    public void markGuaranteedTaken() {
        this.guaranteedTaken = true;
    }

    /** 出口的可读文本，例如 "出口：北→湛恩大道  西→第一食堂" */
    public String exitsText() {
        StringBuilder sb = new StringBuilder("出口：");
        boolean any = false;
        for (int i = 0; i < exits.length; i++) {
            if (exits[i] >= 0) {
                if (any) {
                    sb.append("   ");
                }
                String targetName = exits[i] < CampusData.ROOMS.length
                        ? CampusData.ROOMS[exits[i]].getName()
                        : "？";
                sb.append(DIRECTION_CN[i]).append("→").append(targetName);
                any = true;
            }
        }
        if (!any) {
            sb.append("（没有出口）");
        }
        return sb.toString();
    }

    /** 用 toString 输出场景标题与描述（任务 5 的 Object 要求） */
    @Override
    public String toString() {
        return "【" + name + "】\n" + description;
    }
}
