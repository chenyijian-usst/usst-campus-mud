# 上理江湖 · 沪江迷踪（USST MUD）

一个以上海理工大学军工路校区为背景的**单机文字 MUD**，纯 Java 控制台程序，无第三方依赖。

- 14 个校园场景、8 位 NPC、6 个学业怪物、8 道谜题、8 枚印章
- 走路耗体力、刷题涨学分、体力归零会被强制送回宿舍
- 谜题 / 寻宝 / 回合制对决三条线交织，收集 6 枚印章开启图书馆顶层门禁
- 支线「照片里的 1987」：从旧信箱里的毕业照，追到校门口保安大叔的往事

## 快速开始

Windows 下双击：

```
compile.bat     # 编译
run.bat         # 运行
test.bat        # 运行自测（40 项断言）
```

或者手动执行：

```bat
javac -encoding UTF-8 -d out src\usst\mud\*.java
java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp out usst.mud.Game
```

可选参数：

| 参数 | 作用 |
|---|---|
| `--seed=7` | 固定随机种子，复现同一场随机事件 / 谜题顺序 |
| `-Dmud.echo=true` | 回显玩家输入，便于把运行过程写进报告 |

## 目录结构

```
├─ src/usst/mud/          游戏源码（13 个类）
│  ├─ Game.java           主循环、时间推进、场景描述、战斗调度
│  ├─ Player.java         玩家：体力 / 学分 / 行动 / 背包，含透支联动
│  ├─ Room.java           场景：描述、六个方向的出口数组
│  ├─ CampusData.java     6 个一维对象数组：场景 / NPC / 怪物 / 谜题 / 物品 / 事件
│  ├─ CommandProcessor.java  指令解析（processCommand）
│  ├─ Battle.java         回合制对决
│  ├─ NPC.java            对话与任务发放（支持多段剧情）
│  ├─ TalkStep.java       多段支线中的一段对话
│  ├─ Monster.java        对手
│  ├─ Question.java       谜题与答案判定
│  ├─ Item.java           物品
│  ├─ Inventory.java      背包（数组实现，16 格）
│  └─ CampusEvent.java    随机校园事件
├─ test/usst/mud/SmokeTest.java   不启动游戏的自测类
├─ demo1.txt ~ demo5.txt  五份脚本化输入
├─ run1.txt ~ run5.txt    对应的完整运行实录
├─ compile.bat / run.bat / test.bat
├─ README.md
└─ 课程报告.md
```

## 指令

| 指令 | 别名 | 说明 |
|---|---|---|
| `help` | `?` `h` `帮助` | 指令列表 |
| `look` | `l` `看` | 观察当前场景 |
| `go 方向` | `走` `去` | 方向支持 `north/south/east/west/up/down`、`n/s/e/w/u/d`、`北南东西上下` |
| `status` | `状态` | 角色状态面板 |
| `map` | `地图` | 全图列表 |
| `bag` | `背包` `inv` | 背包与印章数 |
| `talk 名字` | `对话` | 与 NPC 交谈、交任务 |
| `search` | `搜` | 搜索场景，可能找到物品或印章 |
| `quiz` / `answer 答案` / `hint` | `答题` `答` `提示` | 谜题三件套 |
| `study` | `自习` `刷题` | 消耗 25 体力换 2~4 学分 |
| `rest` | `休息` | 在宿舍 / 食堂 / 校医院恢复 30 体力 |
| `fight` | `挑战` | 进入回合制对决 |
| `attack` / `defend` / `flee` | `攻击` `防御` `逃跑` | 战斗指令 |
| `use 物品` | `用` | 使用食物 / 道具 |
| `quit` | `退出` `q` | 退出 |

指令不区分大小写，首尾空格自动忽略；输入空行、未知指令、`go` 不带方向都不会崩溃。

## 数值规则

| 属性 | 范围 | 规则 |
|---|---|---|
| 体力 energy | 0 ~ 100 | 移动 -5，被怪物击中扣血，刷题 -25；**归零时自动送回宿舍、行动 -5、体力恢复到 20** |
| 学分 credit | 0 ~ 100 | 答题 +3~6、战斗 +6~20、随机事件；用 `Math.max/min` 夹紧，按区间换算等级 |
| 行动 actionsLeft | 0 ~ 30 / 天 | 每次主动行为 -1；用尽后强制进入下一天 |
| 时间 | 06:00 ~ 24:00 | 每种行为消耗 5~30 分钟；24:00 自动睡觉，次日 06:00 刷新 |

## 一局怎么玩

1. 在校门口与**保安大叔**对话，拿到第一枚印章；
2. 去**教育超市**搜出猫粮，回**湛恩大道**喂校猫，换第二枚；
3. 在**思裴堂**搜索旧信箱、在**思晏堂**击败四六级词灵、在**格致堂**击败高数幽灵，各得一枚；
   - 顺手在思裴堂第二次搜索，可以触发支线「照片里的 1987」；
4. 答对 2 题找**社团学长**、答对 4 题找**图书馆管理员**，再得两枚；
5. 集齐 6 枚后上图书馆顶层，击败「选课系统·熵」通关。

## 设计要点（对应课程任务）

| 课程要求 | 实现 |
|---|---|
| 至少 3 个主要类及职责 | `Game` / `Player` / `Room`，见报告第 2 节 |
| 一条指令的完整链路 | `go north`：`start()` → `processCommand()` → `Game.move()` → `Player.moveTo()` → `look()` |
| 指出代码问题 | 静态数组里混入可变状态、`Game` 与 `System.in` 强耦合，见报告 2.3 |
| 真正的封装 | `Player` 与 `Room` 全部字段 private，构造器校验，业务方法维护边界 |
| 体力归零联动 | `Player.consumeEnergy()` 内部调用私有 `collapse()` |
| 一维对象数组 | `CampusData` 中 6 个数组，共 60 个对象 |
| 数组遍历 / 查找 | `look()` 遍历 NPC 与怪物，`itemById()`、`findNpc()`、`countOfType()` |
| 独立命令处理 | `CommandProcessor.processCommand()` |
| Random | 随机事件、抽题、掉落、战斗暴击、逃跑 |
| Math | 体力 / 学分 / 伤害的上下界 |
| String | `trim`、`split`、`toLowerCase`、`equalsIgnoreCase`、`contains`、`replace` |
| toString | `Player`、`Room`、`Item`、`Monster` 均重写 |
| 多段剧情 | `NPC` + `TalkStep`，支线阶段存在 `Player` 的 `questStages` 里 |

## 自测

`test.bat` 会直接构造 `Player` / `Battle` 对象，验证 40 条规则（体力夹紧、学分上下限、出口合法性、判题容错、背包容量、支线阶段流转、Boss 战），不依赖控制台输入。
