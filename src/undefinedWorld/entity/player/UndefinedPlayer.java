package undefinedWorld.entity.player;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import doublePlugin.entity.player.NewOfflinePlayer;
import doublePlugin.entity.player.NewPlayer;
import doublePlugin.entity.player.PlayerInfoMaps;
import doublePlugin.scheduler.RunnableEx;
import doublePlugin.util.CoolTime;
import doublePlugin.util.map.InfoMaps;
import doublePlugin.util.map.NotNullStrMap;
import doublePlugin.util.scoreBoard.ScoreBoardHelper;
import undefinedWorld.UndefinedWorld;
import undefinedWorld.item.equipment.Equipment;
import undefinedWorld.entity.player.attribute.PlayerAttribute;
import undefinedWorld.entity.player.attribute.PlayerAttribute.Attributes;
import undefinedWorld.entity.player.equipment.PlayerEquipment;
import undefinedWorld.entity.player.equipment.PlayerEquipment.EquipmentSlot;
import undefinedWorld.entity.player.invenrtory.PlayerInvMaker;
import undefinedWorld.entity.player.level.Level;
import undefinedWorld.entity.player.stat.PlayerStat;
import undefinedWorld.entity.player.stat.Stats;

public class UndefinedPlayer {
    private static final HashMap<UUID, UndefinedPlayer> map = new HashMap<>();
    private static final String LEVEL = "레벨";
    private static final String EXP = "경험치";

    public static UndefinedPlayer getUndefinedPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if (!map.containsKey(uuid)) {
            map.put(uuid, new UndefinedPlayer(NewPlayer.getNewPlayer(player)));
        }

        return map.get(uuid);
    }

    public static UndefinedPlayer getUndefinedPlayer(NewPlayer player) {
        UUID uuid = player.getUniqueId();

        if (!map.containsKey(uuid)) {
            map.put(uuid, new UndefinedPlayer(player));
        }

        return map.get(uuid);
    }

    public static boolean containsUndefinedPlayer(Player player) {
        return map.containsKey(player.getUniqueId());
    }

    public static boolean containsUndefinedPlayer(NewPlayer player) {
        return map.containsKey(player.getUniqueId());
    }

    private static final Equipment equipment = new Equipment();
    NewPlayer player;
    PlayerAttribute playerAttribute;
    PlayerStat playerStat;
    PlayerEquipment playerEquipment;

    private UndefinedPlayer(NewPlayer player) {
        this.player = player;
        this.playerAttribute = new PlayerAttribute(player);
        this.playerStat = new PlayerStat(player);
        this.playerEquipment = new PlayerEquipment(player);
        ScoreBoardHelper scoreBoard = player.getScoreBoardHelper();
        if(scoreBoard == null) {
            scoreBoard = new ScoreBoardHelper(player.getPlayer());
        }
        scoreBoard.setTitle("§fCastrum Server");
        scoreBoard.setSlot(5, "§f§m                          ");
        scoreBoard.setSlot(4, "§7Player§f: " + player.getName());
        scoreBoard.setSlot(3, "§dLevel§f: 1");
        scoreBoard.setSlot(2, "§2Exp§f: 0/0");
        scoreBoard.setSlot(1, "§f§m                          ");
        player.setScoreBoardHelper(scoreBoard);

        player.putRunnable(player.getUniqueId().toString() + " Runnable", new RunnableEx() {
            @Override
            public void function() {
                double mana = getAttribute(Attributes.MANA);
                double mana_max = getAttribute(Attributes.MANA_MAX);
                double nowHealth = Math.round(player.getHealth());
                double maxHealth = Math.round(player.getMaxHealth());
                int level = getUndefinedLevel();
                int exp = getUndeinfedExp();
                int maxExp = getMaxExp();

                player.sendActionBar("§c『§fHealth§c』 §7" + nowHealth + "/" + maxHealth + "   §9『§fMana§9』 §7" + (Math.round(mana * 100) / 100D) + "/" + (Math.round(mana_max * 100) / 100D));


                if(this.getCount() % 5 == 0) {
                    double heal = (maxHealth * getAttribute(Attributes.HEALTH_REGEN) * 0.25D * 0.01D) + nowHealth;
                    heal = Math.round(heal * 1000) / 1000;

                    if (heal <= maxHealth) {
                        player.setHealth(heal);
                    } else {
                        player.setHealth(maxHealth);
                    }

                    double addMana = Math.round(((getAttribute(Attributes.MANA_REGEN) * 0.25D) + mana) * 100D) / 100D;

                    if (addMana <= mana_max) {
                        setAttribute(Attributes.MANA, addMana);
                    } else {
                        setAttribute(Attributes.MANA, mana_max);
                    }
                }

                ScoreBoardHelper scoreBoard = player.getScoreBoardHelper();
                scoreBoard.setSlot(3, "§dLevel§f: " + level);
                scoreBoard.setSlot(2, "§2Exp§f: " + exp + "/" + maxExp);


                if(!player.getPlayerConnect() || UndefinedWorld.reload) {
                    removeUndefinedPlayer();
                }
            }
        });
    }

    public void setScoreBoard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = player.getScoreboard();

        if(board == null) {
            board = manager.getNewScoreboard();
        }
    }

    public void setStatAttribute() {
        int str = getStat(Stats.STR);
        int agi = getStat(Stats.AGI);
        int hel = getStat(Stats.HEL);
        int imt = getStat(Stats.INT);
        int cri = getStat(Stats.CRI);
        int lck = getStat(Stats.LCK);
        int vam = getStat(Stats.VAM);
        int spi = getStat(Stats.SPI);
        int level = getUndefinedLevel();

        setAttribute(Attributes.PHYSICAL_POWER, (str * 3) + (level * 2));
        setAttribute(Attributes.MAGIC_POWER, (imt * 3) + (level * 2));
        setAttribute(Attributes.PHYSICAL_DEFENSE, (hel * 2));
        setAttribute(Attributes.MAGIC_DEFENSE, (hel * 2));
        setAttribute(Attributes.SPEED, (agi * 1) + +(level * 0.5));
        player.setWalkSpeed(0.2F + ((agi * 1F) + (level * 0.5F)) * 0.01F * 0.2F);
        setAttribute(Attributes.DODGE, (agi * 0.5) + (lck * 0.1));
        setAttribute(Attributes.HEALTH, (str * 10) + (hel * 40) + (level * 20));
        player.setMaxHealth((str * 10) + (hel * 40) + (level * 20));
        setAttribute(Attributes.VAMFIRE, vam * 0.03);
        setAttribute(Attributes.CRITICAL_DAMAGE, (cri * 3));
        setAttribute(Attributes.CRITICAL_PERCENTAGE, (cri) + (lck * 0.3));
        setAttribute(Attributes.MANA_MAX, (imt * 20) + (spi * 30) + 100 + (level * 10));
        setAttribute(Attributes.MANA_REGEN, (spi) + 2 + (level * 0.5D));
        setAttribute(Attributes.HEALTH_REGEN, (level * 0.005D));
        setAttribute(Attributes.ITEM_DROP, lck);
    }

    public void setEquipmentAttribute() {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = this.getEquipment(slot);

            if(equipment.checkEquipment(item)) {
                String type = equipment.getItemType(item);
                String detailType = equipment.getItemDetailType(item);
                if(slot.getType().equals(detailType) || slot.getType().equals(type)) {

                }
            }
        }
    }

    public int getMaxExp() {
        return Level.getMaxExp(getUndefinedLevel());
    }

    public void checkLevelUp() {
        if (getUndefinedLevel() > 100) {
            return;
        }

        while (getUndeinfedExp() >= getMaxExp()) {
            int level = getUndefinedLevel();
            addUndeinfedExp(-getMaxExp());
            addUndefinedLevel(1);
            sendTitle("§6[ §fLEVEL UP §6]", "§7" + level + "   ->   " + (level + 1));
            addStat(Stats.STATPOINT, 3);
        }
    }

    public boolean checkItemLimit(ItemStack itemStack) {
        if(!equipment.checkEquipment(itemStack)) {
            return true;
        }

        for(Stats stat : Stats.values()) {
            int limit = equipment.getLimit(itemStack, stat);
            if(limit > this.getStat(stat)) {
                return false;
            }
        }

        return true;
    }

    public void removeUndefinedPlayer() {
        map.remove(this.player.getUniqueId());
    }

    public int getUndefinedLevel() {
        return player.getIntegerValue(LEVEL);
    }

    public void addUndefinedLevel(int value) {
        player.addIntegerValue(LEVEL, value);
    }

    public void setUndefinedLevel(int value) {
        player.setIntegerValue(LEVEL, value);
    }

    public int getUndeinfedExp() {
        return player.getIntegerValue(EXP);
    }

    public void addUndeinfedExp(int value) {
        player.addIntegerValue(EXP, value);
    }

    public void setUndeinfedExp(int value) {
        player.setIntegerValue(EXP, value);
    }

    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        playerEquipment.setEquipment(slot, item);
    }

    public ItemStack getEquipment(EquipmentSlot slot) {
        return playerEquipment.getEquipment(slot);
    }

    public PlayerInvMaker getEquipmentInvMaker() {
        return playerEquipment.getInvMaker();
    }

    public void saveEquipment() {
        playerEquipment.save();
    }

    public void addAttribute(Attributes attribute, double value) {
        playerAttribute.addAttribute(attribute, value);
    }

    public void setAttribute(Attributes attribute, double value) {
        playerAttribute.setAttribute(attribute, value);
    }

    public double getAttribute(Attributes attribute) {
        return playerAttribute.getAttribute(attribute);
    }

    public void clearAttribute() {
        playerAttribute.clearAttribute();
    }

    public void addStat(Stats stat, int value) {
        playerStat.addStat(stat, value);
    }

    public void setStat(Stats stat, int value) {
        playerStat.setStat(stat, value);
    }

    public int getStat(Stats stat) {
        return playerStat.getStat(stat);
    }

    public void clearStat() {
        playerStat.clearStat();
        setStatAttribute();
    }

    public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
        player.abandonConversation(arg0, arg1);
    }

    public void abandonConversation(Conversation arg0) {
        player.abandonConversation(arg0);
    }

    public void acceptConversationInput(String arg0) {
        player.acceptConversationInput(arg0);
    }

    public void addAtkNum() {
        player.addAtkNum();
    }

    public void addAtkNum(int value) {
        player.addAtkNum(value);
    }

    public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
        return player.addAttachment(arg0, arg1);
    }

    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
        return player.addAttachment(arg0, arg1, arg2, arg3);
    }

    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
        return player.addAttachment(arg0, arg1, arg2);
    }

    public PermissionAttachment addAttachment(Plugin arg0) {
        return player.addAttachment(arg0);
    }

    public void addDefNum() {
        player.addDefNum();
    }

    public void addDefNum(int value) {
        player.addDefNum(value);
    }

    public void addDoubleValue(String key, double value) {
        player.addDoubleValue(key, value);
    }

    public void addIntegerValue(String key, int value) {
        player.addIntegerValue(key, value);
    }

    public void addLocationValue(String key, Location value) {
        player.addLocationValue(key, value);
    }

    public void addLocationValue(String key, Vector vec) {
        player.addLocationValue(key, vec);
    }

    public void addLongValue(String key, long value) {
        player.addLongValue(key, value);
    }

    public boolean addPassenger(Entity arg0) {
        return player.addPassenger(arg0);
    }

    public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
        return player.addPotionEffect(arg0, arg1);
    }

    public boolean addPotionEffect(PotionEffect arg0) {
        return player.addPotionEffect(arg0);
    }

    public boolean addPotionEffects(Collection<PotionEffect> arg0) {
        return player.addPotionEffects(arg0);
    }

    public boolean addScoreboardTag(String arg0) {
        return player.addScoreboardTag(arg0);
    }

    public void addStringValue(String key, String value) {
        player.addStringValue(key, value);
    }

    public void attack(Entity arg0) {
        player.attack(arg0);
    }

    public boolean beginConversation(Conversation arg0) {
        return player.beginConversation(arg0);
    }

    public boolean canSee(Player arg0) {
        return player.canSee(arg0);
    }

    public void chat(String arg0) {
        player.chat(arg0);
    }

    public boolean checkAtkNum(int value) {
        return player.checkAtkNum(value);
    }

    public boolean checkCoolTime(String coolTimeName) {
        return player.checkCoolTime(coolTimeName);
    }

    public boolean checkDefNum(int value) {
        return player.checkDefNum(value);
    }

    public void closeInventory() {
        player.closeInventory();
    }

    public boolean containsBooleanValue(String key) {
        return player.containsBooleanValue(key);
    }

    public boolean containsDoubleValue(String key) {
        return player.containsDoubleValue(key);
    }

    public boolean containsIntegerValue(String key) {
        return player.containsIntegerValue(key);
    }

    public boolean containsInventoryValue(String key) {
        return player.containsInventoryValue(key);
    }

    public boolean containsItemStackValue(String key) {
        return player.containsItemStackValue(key);
    }

    public boolean containsLocationValue(String key) {
        return player.containsLocationValue(key);
    }

    public boolean containsLongValue(String key) {
        return player.containsLongValue(key);
    }

    public boolean containsStringValue(String key) {
        return player.containsStringValue(key);
    }

    public void damage(double arg0, Entity arg1) {
        player.damage(arg0, arg1);
    }

    public void damage(double arg0) {
        player.damage(arg0);
    }

    public void decrementStatistic(Statistic arg0, EntityType arg1, int arg2) {
        player.decrementStatistic(arg0, arg1, arg2);
    }

    public void decrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
        player.decrementStatistic(arg0, arg1);
    }

    public void decrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
        player.decrementStatistic(arg0, arg1);
    }

    public void decrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
        player.decrementStatistic(arg0, arg1, arg2);
    }

    public void decrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
        player.decrementStatistic(arg0, arg1);
    }

    public void decrementStatistic(Statistic arg0) throws IllegalArgumentException {
        player.decrementStatistic(arg0);
    }

    public boolean discoverRecipe(NamespacedKey arg0) {
        return player.discoverRecipe(arg0);
    }

    public int discoverRecipes(Collection<NamespacedKey> arg0) {
        return player.discoverRecipes(arg0);
    }

    public boolean dropItem(boolean arg0) {
        return player.dropItem(arg0);
    }

    public boolean eject() {
        return player.eject();
    }

    public double getAbsorptionAmount() {
        return player.getAbsorptionAmount();
    }

    public Collection<PotionEffect> getActivePotionEffects() {
        return player.getActivePotionEffects();
    }

    public InetSocketAddress getAddress() {
        return player.getAddress();
    }

    public AdvancementProgress getAdvancementProgress(Advancement arg0) {
        return player.getAdvancementProgress(arg0);
    }

    public boolean getAllowFlight() {
        return player.getAllowFlight();
    }

    public int getArrowCooldown() {
        return player.getArrowCooldown();
    }

    public int getArrowsInBody() {
        return player.getArrowsInBody();
    }

    public int getAtkNum() {
        return player.getAtkNum();
    }

    public float getAttackCooldown() {
        return player.getAttackCooldown();
    }

    public AttributeInstance getAttribute(Attribute arg0) {
        return player.getAttribute(arg0);
    }

    public Location getBedLocation() {
        return player.getBedLocation();
    }

    public Location getBedSpawnLocation() {
        return player.getBedSpawnLocation();
    }

    public Set<String> getBooleanKeys() {
        return player.getBooleanKeys();
    }

    public NotNullStrMap<Boolean> getBooleanMap() {
        return player.getBooleanMap();
    }

    public boolean getBooleanValue(String key) {
        return player.getBooleanValue(key);
    }

    public Collection<Boolean> getBooleanValues() {
        return player.getBooleanValues();
    }

    public BoundingBox getBoundingBox() {
        return player.getBoundingBox();
    }

    public boolean getCanPickupItems() {
        return player.getCanPickupItems();
    }

    public EntityCategory getCategory() {
        return player.getCategory();
    }

    public int getClientViewDistance() {
        return player.getClientViewDistance();
    }

    public Set<UUID> getCollidableExemptions() {
        return player.getCollidableExemptions();
    }

    public Location getCompassTarget() {
        return player.getCompassTarget();
    }

    public int getCooldown(Material arg0) {
        return player.getCooldown(arg0);
    }

    public String getCustomName() {
        return player.getCustomName();
    }

    public int getDefNum() {
        return player.getDefNum();
    }

    public Set<NamespacedKey> getDiscoveredRecipes() {
        return player.getDiscoveredRecipes();
    }

    public String getDisplayName() {
        return player.getDisplayName();
    }

    public Set<String> getDoubleKeys() {
        return player.getDoubleKeys();
    }

    public NotNullStrMap<Double> getDoubleMap() {
        return player.getDoubleMap();
    }

    public double getDoubleValue(String key) {
        return player.getDoubleValue(key);
    }

    public Collection<Double> getDoubleValues() {
        return player.getDoubleValues();
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return player.getEffectivePermissions();
    }

    public Inventory getEnderChest() {
        return player.getEnderChest();
    }

    public int getEntityId() {
        return player.getEntityId();
    }

    public EntityEquipment getEquipment() {
        return player.getEquipment();
    }

    public float getExhaustion() {
        return player.getExhaustion();
    }

    public float getExp() {
        return player.getExp();
    }

    public int getExpToLevel() {
        return player.getExpToLevel();
    }

    public double getEyeHeight() {
        return player.getEyeHeight();
    }

    public double getEyeHeight(boolean arg0) {
        return player.getEyeHeight(arg0);
    }

    public Location getEyeLocation() {
        return player.getEyeLocation();
    }

    public BlockFace getFacing() {
        return player.getFacing();
    }

    public float getFallDistance() {
        return player.getFallDistance();
    }

    public int getFireTicks() {
        return player.getFireTicks();
    }

    public long getFirstPlayed() {
        return player.getFirstPlayed();
    }

    public float getFlySpeed() {
        return player.getFlySpeed();
    }

    public int getFoodLevel() {
        return player.getFoodLevel();
    }

    public GameMode getGameMode() {
        return player.getGameMode();
    }

    public double getHealth() {
        return player.getHealth();
    }

    public double getHealthScale() {
        return player.getHealthScale();
    }

    public double getHeight() {
        return player.getHeight();
    }

    public Set<String> getIntegerKeys() {
        return player.getIntegerKeys();
    }

    public NotNullStrMap<Integer> getIntegerMap() {
        return player.getIntegerMap();
    }

    public int getIntegerValue(String key) {
        return player.getIntegerValue(key);
    }

    public Collection<Integer> getIntegerValues() {
        return player.getIntegerValues();
    }

    public PlayerInventory getInventory() {
        return player.getInventory();
    }

    public Set<String> getInventoryKeys() {
        return player.getInventoryKeys();
    }

    public NotNullStrMap<Inventory> getInventoryMap() {
        return player.getInventoryMap();
    }

    public Inventory getInventoryValue(String key) {
        return player.getInventoryValue(key);
    }

    public Collection<Inventory> getInventoryValues() {
        return player.getInventoryValues();
    }

    public ItemStack getItemInHand() {
        return player.getItemInHand();
    }

    public ItemStack getItemOnCursor() {
        return player.getItemOnCursor();
    }

    public Set<String> getItemStackKeys() {
        return player.getItemStackKeys();
    }

    public NotNullStrMap<ItemStack> getItemStackMap() {
        return player.getItemStackMap();
    }

    public ItemStack getItemStackValue(String key) {
        return player.getItemStackValue(key);
    }

    public Collection<ItemStack> getItemStackValues() {
        return player.getItemStackValues();
    }

    public Player getKiller() {
        return player.getKiller();
    }

    public double getLastDamage() {
        return player.getLastDamage();
    }

    public EntityDamageEvent getLastDamageCause() {
        return player.getLastDamageCause();
    }

    public long getLastPlayed() {
        return player.getLastPlayed();
    }

    public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
        return player.getLastTwoTargetBlocks(arg0, arg1);
    }

    public Entity getLeashHolder() throws IllegalStateException {
        return player.getLeashHolder();
    }

    public double getLessCoolTime(String coolTimeName) {
        return player.getLessCoolTime(coolTimeName);
    }

    public int getLevel() {
        return player.getLevel();
    }

    public List<Block> getLineOfSight(Set<Material> arg0, int arg1) {
        return player.getLineOfSight(arg0, arg1);
    }

    public Set<String> getListeningPluginChannels() {
        return player.getListeningPluginChannels();
    }

    public String getLocale() {
        return player.getLocale();
    }

    public Location getLocation() {
        return player.getLocation();
    }

    public Location getLocation(Location arg0) {
        return player.getLocation(arg0);
    }

    public Set<String> getLocationKeys() {
        return player.getLocationKeys();
    }

    public NotNullStrMap<Location> getLocationMap() {
        return player.getLocationMap();
    }

    public Location getLocationValue(String key) {
        return player.getLocationValue(key);
    }

    public Collection<Location> getLocationValues() {
        return player.getLocationValues();
    }

    public Set<String> getLongKeys() {
        return player.getLongKeys();
    }

    public NotNullStrMap<Long> getLongMap() {
        return player.getLongMap();
    }

    public long getLongValue(String key) {
        return player.getLongValue(key);
    }

    public Collection<Long> getLongValues() {
        return player.getLongValues();
    }

    public MainHand getMainHand() {
        return player.getMainHand();
    }

    public int getMaxFireTicks() {
        return player.getMaxFireTicks();
    }

    public double getMaxHealth() {
        return player.getMaxHealth();
    }

    public int getMaximumAir() {
        return player.getMaximumAir();
    }

    public int getMaximumNoDamageTicks() {
        return player.getMaximumNoDamageTicks();
    }

    public <T> T getMemory(MemoryKey<T> arg0) {
        return player.getMemory(arg0);
    }

    public List<MetadataValue> getMetadata(String arg0) {
        return player.getMetadata(arg0);
    }

    public String getName() {
        return player.getName();
    }

    public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
        return player.getNearbyEntities(arg0, arg1, arg2);
    }

    public NewPlayer getNewPlayer() {
        return player.getNewPlayer();
    }

    public int getNoDamageTicks() {
        return player.getNoDamageTicks();
    }

    public InventoryView getOpenInventory() {
        return player.getOpenInventory();
    }

    public Entity getPassenger() {
        return player.getPassenger();
    }

    public List<Entity> getPassengers() {
        return player.getPassengers();
    }

    public PersistentDataContainer getPersistentDataContainer() {
        return player.getPersistentDataContainer();
    }

    public PistonMoveReaction getPistonMoveReaction() {
        return player.getPistonMoveReaction();
    }

    public Player getPlayer() {
        return player.getPlayer();
    }

    public boolean getPlayerConnect() {
        return player.getPlayerConnect();
    }

    public int getPlayerJoinCount() {
        return player.getPlayerJoinCount();
    }

    public String getPlayerListFooter() {
        return player.getPlayerListFooter();
    }

    public String getPlayerListHeader() {
        return player.getPlayerListHeader();
    }

    public String getPlayerListName() {
        return player.getPlayerListName();
    }

    public long getPlayerTime() {
        return player.getPlayerTime();
    }

    public long getPlayerTimeOffset() {
        return player.getPlayerTimeOffset();
    }

    public WeatherType getPlayerWeather() {
        return player.getPlayerWeather();
    }

    public int getPortalCooldown() {
        return player.getPortalCooldown();
    }

    public Pose getPose() {
        return player.getPose();
    }

    public PotionEffect getPotionEffect(PotionEffectType arg0) {
        return player.getPotionEffect(arg0);
    }

    public int getRemainingAir() {
        return player.getRemainingAir();
    }

    public boolean getRemoveWhenFarAway() {
        return player.getRemoveWhenFarAway();
    }

    public Runnable getRunnable(String code) {
        return player.getRunnable(code);
    }

    public float getSaturation() {
        return player.getSaturation();
    }

    public Scoreboard getScoreboard() {
        return player.getScoreboard();
    }

    public Set<String> getScoreboardTags() {
        return player.getScoreboardTags();
    }

    public Server getServer() {
        return player.getServer();
    }

    public Entity getShoulderEntityLeft() {
        return player.getShoulderEntityLeft();
    }

    public Entity getShoulderEntityRight() {
        return player.getShoulderEntityRight();
    }

    public int getSleepTicks() {
        return player.getSleepTicks();
    }

    public Entity getSpectatorTarget() {
        return player.getSpectatorTarget();
    }

    public int getStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
        return player.getStatistic(arg0, arg1);
    }

    public int getStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
        return player.getStatistic(arg0, arg1);
    }

    public int getStatistic(Statistic arg0) throws IllegalArgumentException {
        return player.getStatistic(arg0);
    }

    public Set<String> getStringKeys() {
        return player.getStringKeys();
    }

    public NotNullStrMap<String> getStringMap() {
        return player.getStringMap();
    }

    public String getStringValue(String key) {
        return player.getStringValue(key);
    }

    public Collection<String> getStringValues() {
        return player.getStringValues();
    }

    public Block getTargetBlock(Set<Material> arg0, int arg1) {
        return player.getTargetBlock(arg0, arg1);
    }

    public Block getTargetBlockExact(int arg0, FluidCollisionMode arg1) {
        return player.getTargetBlockExact(arg0, arg1);
    }

    public Block getTargetBlockExact(int arg0) {
        return player.getTargetBlockExact(arg0);
    }

    public int getTicksLived() {
        return player.getTicksLived();
    }

    public int getTotalExperience() {
        return player.getTotalExperience();
    }

    public EntityType getType() {
        return player.getType();
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public String getUniqueIdtoString() {
        return player.getUniqueIdtoString();
    }

    public Entity getVehicle() {
        return player.getVehicle();
    }

    public Vector getVelocity() {
        return player.getVelocity();
    }

    public float getWalkSpeed() {
        return player.getWalkSpeed();
    }

    public double getWidth() {
        return player.getWidth();
    }

    public World getWorld() {
        return player.getWorld();
    }

    public void giveExp(int arg0) {
        player.giveExp(arg0);
    }

    public void giveExpLevels(int arg0) {
        player.giveExpLevels(arg0);
    }

    public boolean hasAI() {
        return player.hasAI();
    }

    public boolean hasCooldown(Material arg0) {
        return player.hasCooldown(arg0);
    }

    public boolean hasDiscoveredRecipe(NamespacedKey arg0) {
        return player.hasDiscoveredRecipe(arg0);
    }

    public boolean hasGravity() {
        return player.hasGravity();
    }

    public boolean hasLineOfSight(Entity arg0) {
        return player.hasLineOfSight(arg0);
    }

    public boolean hasMetadata(String arg0) {
        return player.hasMetadata(arg0);
    }

    public boolean hasPermission(Permission arg0) {
        return player.hasPermission(arg0);
    }

    public boolean hasPermission(String arg0) {
        return player.hasPermission(arg0);
    }

    public boolean hasPlayedBefore() {
        return player.hasPlayedBefore();
    }

    public boolean hasPotionEffect(PotionEffectType arg0) {
        return player.hasPotionEffect(arg0);
    }

    public int hashCode() {
        return player.hashCode();
    }

    public void hidePlayer(Player arg0) {
        player.hidePlayer(arg0);
    }

    public void hidePlayer(Plugin arg0, Player arg1) {
        player.hidePlayer(arg0, arg1);
    }

    public void incrementStatistic(Statistic arg0, EntityType arg1, int arg2) throws IllegalArgumentException {
        player.incrementStatistic(arg0, arg1, arg2);
    }

    public void incrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
        player.incrementStatistic(arg0, arg1);
    }

    public void incrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
        player.incrementStatistic(arg0, arg1);
    }

    public void incrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
        player.incrementStatistic(arg0, arg1, arg2);
    }

    public void incrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
        player.incrementStatistic(arg0, arg1);
    }

    public void incrementStatistic(Statistic arg0) throws IllegalArgumentException {
        player.incrementStatistic(arg0);
    }

    public boolean isBanned() {
        return player.isBanned();
    }

    public boolean isBlocking() {
        return player.isBlocking();
    }

    public boolean isCollidable() {
        return player.isCollidable();
    }

    public boolean isConversing() {
        return player.isConversing();
    }

    public boolean isCustomNameVisible() {
        return player.isCustomNameVisible();
    }

    public boolean isDead() {
        return player.isDead();
    }

    public boolean isEmpty() {
        return player.isEmpty();
    }

    public boolean isFlying() {
        return player.isFlying();
    }

    public boolean isGliding() {
        return player.isGliding();
    }

    public boolean isGlowing() {
        return player.isGlowing();
    }

    public boolean isHandRaised() {
        return player.isHandRaised();
    }

    public boolean isHealthScaled() {
        return player.isHealthScaled();
    }

    public boolean isInWater() {
        return player.isInWater();
    }

    public boolean isInsideVehicle() {
        return player.isInsideVehicle();
    }

    public boolean isInvisible() {
        return player.isInvisible();
    }

    public boolean isInvulnerable() {
        return player.isInvulnerable();
    }

    public boolean isLeashed() {
        return player.isLeashed();
    }

    public boolean isOnGround() {
        return player.isOnGround();
    }

    public boolean isOnline() {
        return player.isOnline();
    }

    public boolean isOp() {
        return player.isOp();
    }

    public boolean isPermissionSet(Permission arg0) {
        return player.isPermissionSet(arg0);
    }

    public boolean isPermissionSet(String arg0) {
        return player.isPermissionSet(arg0);
    }

    public boolean isPersistent() {
        return player.isPersistent();
    }

    public boolean isPlayerTimeRelative() {
        return player.isPlayerTimeRelative();
    }

    public boolean isRiptiding() {
        return player.isRiptiding();
    }

    public boolean isSilent() {
        return player.isSilent();
    }

    public boolean isSleeping() {
        return player.isSleeping();
    }

    public boolean isSleepingIgnored() {
        return player.isSleepingIgnored();
    }

    public boolean isSneaking() {
        return player.isSneaking();
    }

    public boolean isSprinting() {
        return player.isSprinting();
    }

    public boolean isSwimming() {
        return player.isSwimming();
    }

    public boolean isValid() {
        return player.isValid();
    }

    public boolean isWhitelisted() {
        return player.isWhitelisted();
    }

    public void kickPlayer(String arg0) {
        player.kickPlayer(arg0);
    }

    public <T extends Projectile> T launchProjectile(Class<? extends T> arg0, Vector arg1) {
        return player.launchProjectile(arg0, arg1);
    }

    public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
        return player.launchProjectile(arg0);
    }

    public boolean leaveVehicle() {
        return player.leaveVehicle();
    }

    public void loadData() {
        player.loadData();
    }

    public void openBook(ItemStack arg0) {
        player.openBook(arg0);
    }

    public InventoryView openEnchanting(Location arg0, boolean arg1) {
        return player.openEnchanting(arg0, arg1);
    }

    public InventoryView openInventory(Inventory arg0) {
        return player.openInventory(arg0);
    }

    public void openInventory(InventoryView arg0) {
        player.openInventory(arg0);
    }

    public InventoryView openMerchant(Merchant arg0, boolean arg1) {
        return player.openMerchant(arg0, arg1);
    }

    public InventoryView openMerchant(Villager arg0, boolean arg1) {
        return player.openMerchant(arg0, arg1);
    }

    public InventoryView openWorkbench(Location arg0, boolean arg1) {
        return player.openWorkbench(arg0, arg1);
    }

    public boolean performCommand(String arg0) {
        return player.performCommand(arg0);
    }

    public void playEffect(EntityEffect arg0) {
        player.playEffect(arg0);
    }

    public void playEffect(Location arg0, Effect arg1, int arg2) {
        player.playEffect(arg0, arg1, arg2);
    }

    public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
        player.playEffect(arg0, arg1, arg2);
    }

    public void playNote(Location arg0, byte arg1, byte arg2) {
        player.playNote(arg0, arg1, arg2);
    }

    public void playNote(Location arg0, Instrument arg1, Note arg2) {
        player.playNote(arg0, arg1, arg2);
    }

    public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
        player.playSound(arg0, arg1, arg2, arg3);
    }

    public void playSound(Location arg0, Sound arg1, SoundCategory arg2, float arg3, float arg4) {
        player.playSound(arg0, arg1, arg2, arg3, arg4);
    }

    public void playSound(Location arg0, String arg1, float arg2, float arg3) {
        player.playSound(arg0, arg1, arg2, arg3);
    }

    public void playSound(Location arg0, String arg1, SoundCategory arg2, float arg3, float arg4) {
        player.playSound(arg0, arg1, arg2, arg3, arg4);
    }

    public void putRunnable(String code, Runnable run) {
        player.putRunnable(code, run);
    }

    public RayTraceResult rayTraceBlocks(double arg0, FluidCollisionMode arg1) {
        return player.rayTraceBlocks(arg0, arg1);
    }

    public RayTraceResult rayTraceBlocks(double arg0) {
        return player.rayTraceBlocks(arg0);
    }

    public void recalculatePermissions() {
        player.recalculatePermissions();
    }

    public void reduceCoolTime(String coolTimeName, double reduceSecond) {
        player.reduceCoolTime(coolTimeName, reduceSecond);
    }

    public void remove() {
        player.remove();
    }

    public void removeAttachment(PermissionAttachment arg0) {
        player.removeAttachment(arg0);
    }

    public void removeBooleanValue(String key) {
        player.removeBooleanValue(key);
    }

    public void removeCoolTime(String coolTimeName) {
        player.removeCoolTime(coolTimeName);
    }

    public void removeDoubleValue(String key) {
        player.removeDoubleValue(key);
    }

    public void removeIntegerValue(String key) {
        player.removeIntegerValue(key);
    }

    public void removeInventoryValue(String key) {
        player.removeInventoryValue(key);
    }

    public void removeItemStackValue(String key) {
        player.removeItemStackValue(key);
    }

    public void removeLocationValue(String key) {
        player.removeLocationValue(key);
    }

    public void removeLongValue(String key) {
        player.removeLongValue(key);
    }

    public void removeMetadata(String arg0, Plugin arg1) {
        player.removeMetadata(arg0, arg1);
    }

    public void removeNewPlayer() {
        player.removeNewPlayer();
    }

    public boolean removePassenger(Entity arg0) {
        return player.removePassenger(arg0);
    }

    public void removePotionEffect(PotionEffectType arg0) {
        player.removePotionEffect(arg0);
    }

    public boolean removeScoreboardTag(String arg0) {
        return player.removeScoreboardTag(arg0);
    }

    public void removeStringValue(String key) {
        player.removeStringValue(key);
    }

    public void resetMaxHealth() {
        player.resetMaxHealth();
    }

    public void resetPlayerTime() {
        player.resetPlayerTime();
    }

    public void resetPlayerWeather() {
        player.resetPlayerWeather();
    }

    public void resetTitle() {
        player.resetTitle();
    }

    public void saveData() {
        player.saveData();
    }

    public void saveInfoMaps() {
        player.saveInfoMaps();
    }

    public void sendActionBar(String message) {
        player.sendActionBar(message);
    }

    public void sendBlockChange(Location arg0, BlockData arg1) {
        player.sendBlockChange(arg0, arg1);
    }

    public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
        player.sendBlockChange(arg0, arg1, arg2);
    }

    public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
        return player.sendChunkChange(arg0, arg1, arg2, arg3, arg4);
    }

    public void sendExperienceChange(float arg0, int arg1) {
        player.sendExperienceChange(arg0, arg1);
    }

    public void sendExperienceChange(float arg0) {
        player.sendExperienceChange(arg0);
    }

    public void sendMap(MapView arg0) {
        player.sendMap(arg0);
    }

    public void sendMessage(String arg0) {
        player.sendMessage(arg0);
    }

    public void sendMessage(String[] arg0) {
        player.sendMessage(arg0);
    }

    public void sendMessage(UUID arg0, String arg1) {
        player.sendMessage(arg0, arg1);
    }

    public void sendMessage(UUID arg0, String[] arg1) {
        player.sendMessage(arg0, arg1);
    }

    public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
        player.sendPluginMessage(arg0, arg1, arg2);
    }

    public void sendRawMessage(String arg0) {
        player.sendRawMessage(arg0);
    }

    public void sendRawMessage(UUID arg0, String arg1) {
        player.sendRawMessage(arg0, arg1);
    }

    public void sendSignChange(Location arg0, String[] arg1, DyeColor arg2) throws IllegalArgumentException {
        player.sendSignChange(arg0, arg1, arg2);
    }

    public void sendSignChange(Location arg0, String[] arg1) throws IllegalArgumentException {
        player.sendSignChange(arg0, arg1);
    }

    public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {
        player.sendTitle(arg0, arg1, arg2, arg3, arg4);
    }

    public void sendTitle(String arg0, String arg1) {
        player.sendTitle(arg0, arg1);
    }

    public Map<String, Object> serialize() {
        return player.serialize();
    }

    public void setAI(boolean arg0) {
        player.setAI(arg0);
    }

    public void setAbsorptionAmount(double arg0) {
        player.setAbsorptionAmount(arg0);
    }

    public void setAllowFlight(boolean arg0) {
        player.setAllowFlight(arg0);
    }

    public void setArrowCooldown(int arg0) {
        player.setArrowCooldown(arg0);
    }

    public void setArrowsInBody(int arg0) {
        player.setArrowsInBody(arg0);
    }

    public void setBedSpawnLocation(Location arg0, boolean arg1) {
        player.setBedSpawnLocation(arg0, arg1);
    }

    public void setBedSpawnLocation(Location arg0) {
        player.setBedSpawnLocation(arg0);
    }

    public void setBooleanValue(String key, boolean value) {
        player.setBooleanValue(key, value);
    }

    public void setCanPickupItems(boolean arg0) {
        player.setCanPickupItems(arg0);
    }

    public void setCollidable(boolean arg0) {
        player.setCollidable(arg0);
    }

    public void setCompassTarget(Location arg0) {
        player.setCompassTarget(arg0);
    }

    public void setCoolTimeDay(String coolTimeName, int day) {
        player.setCoolTimeDay(coolTimeName, day);
    }

    public void setCoolTimeHour(String coolTimeName, int hour) {
        player.setCoolTimeHour(coolTimeName, hour);
    }

    public void setCoolTimeMinute(String coolTimeName, int minute) {
        player.setCoolTimeMinute(coolTimeName, minute);
    }

    public void setCoolTimeMonth(String coolTimeName, int month) {
        player.setCoolTimeMonth(coolTimeName, month);
    }

    public void setCoolTimeSecond(String coolTimeName, double second) {
        player.setCoolTimeSecond(coolTimeName, second);
    }

    public void setCoolTimeYear(String coolTimeName, int second) {
        player.setCoolTimeYear(coolTimeName, second);
    }

    public void setCooldown(Material arg0, int arg1) {
        player.setCooldown(arg0, arg1);
    }

    public void setCustomName(String arg0) {
        player.setCustomName(arg0);
    }

    public void setCustomNameVisible(boolean arg0) {
        player.setCustomNameVisible(arg0);
    }

    public void setDisplayName(String arg0) {
        player.setDisplayName(arg0);
    }

    public void setDoubleValue(String key, double value) {
        player.setDoubleValue(key, value);
    }

    public void setExhaustion(float arg0) {
        player.setExhaustion(arg0);
    }

    public void setExp(float arg0) {
        player.setExp(arg0);
    }

    public void setFallDistance(float arg0) {
        player.setFallDistance(arg0);
    }

    public void setFireTicks(int arg0) {
        player.setFireTicks(arg0);
    }

    public void setFlySpeed(float arg0) throws IllegalArgumentException {
        player.setFlySpeed(arg0);
    }

    public void setFlying(boolean arg0) {
        player.setFlying(arg0);
    }

    public void setFoodLevel(int arg0) {
        player.setFoodLevel(arg0);
    }

    public void setGameMode(GameMode arg0) {
        player.setGameMode(arg0);
    }

    public void setGliding(boolean arg0) {
        player.setGliding(arg0);
    }

    public void setGlowing(boolean arg0) {
        player.setGlowing(arg0);
    }

    public void setGravity(boolean arg0) {
        player.setGravity(arg0);
    }

    public void setHealth(double arg0) {
        player.setHealth(arg0);
    }

    public void setHealthScale(double arg0) throws IllegalArgumentException {
        player.setHealthScale(arg0);
    }

    public void setHealthScaled(boolean arg0) {
        player.setHealthScaled(arg0);
    }

    public void setIntegerValue(String key, int value) {
        player.setIntegerValue(key, value);
    }

    public void setInventoryValue(String key, Inventory value) {
        player.setInventoryValue(key, value);
    }

    public void setInvisible(boolean arg0) {
        player.setInvisible(arg0);
    }

    public void setInvulnerable(boolean arg0) {
        player.setInvulnerable(arg0);
    }

    public void setItemInHand(ItemStack arg0) {
        player.setItemInHand(arg0);
    }

    public void setItemOnCursor(ItemStack arg0) {
        player.setItemOnCursor(arg0);
    }

    public void setItemStackValue(String key, ItemStack value) {
        player.setItemStackValue(key, value);
    }

    public void setLastDamage(double arg0) {
        player.setLastDamage(arg0);
    }

    public void setLastDamageCause(EntityDamageEvent arg0) {
        player.setLastDamageCause(arg0);
    }

    public boolean setLeashHolder(Entity arg0) {
        return player.setLeashHolder(arg0);
    }

    public void setLevel(int arg0) {
        player.setLevel(arg0);
    }

    public void setLocationValue(String key, Location value) {
        player.setLocationValue(key, value);
    }

    public void setLongValue(String key, long value) {
        player.setLongValue(key, value);
    }

    public void setMaxHealth(double arg0) {
        player.setMaxHealth(arg0);
    }

    public void setMaximumAir(int arg0) {
        player.setMaximumAir(arg0);
    }

    public void setMaximumNoDamageTicks(int arg0) {
        player.setMaximumNoDamageTicks(arg0);
    }

    public <T> void setMemory(MemoryKey<T> arg0, T arg1) {
        player.setMemory(arg0, arg1);
    }

    public void setMetadata(String arg0, MetadataValue arg1) {
        player.setMetadata(arg0, arg1);
    }

    public void setNoDamageTicks(int arg0) {
        player.setNoDamageTicks(arg0);
    }

    public void setOp(boolean arg0) {
        player.setOp(arg0);
    }

    public boolean setPassenger(Entity arg0) {
        return player.setPassenger(arg0);
    }

    public void setPersistent(boolean arg0) {
        player.setPersistent(arg0);
    }

    public void setPlayerListFooter(String arg0) {
        player.setPlayerListFooter(arg0);
    }

    public void setPlayerListHeader(String arg0) {
        player.setPlayerListHeader(arg0);
    }

    public void setPlayerListHeaderFooter(String arg0, String arg1) {
        player.setPlayerListHeaderFooter(arg0, arg1);
    }

    public void setPlayerListName(String arg0) {
        player.setPlayerListName(arg0);
    }

    public void setPlayerTime(long arg0, boolean arg1) {
        player.setPlayerTime(arg0, arg1);
    }

    public void setPlayerWeather(WeatherType arg0) {
        player.setPlayerWeather(arg0);
    }

    public void setPortalCooldown(int arg0) {
        player.setPortalCooldown(arg0);
    }

    public void setRemainingAir(int arg0) {
        player.setRemainingAir(arg0);
    }

    public void setRemoveWhenFarAway(boolean arg0) {
        player.setRemoveWhenFarAway(arg0);
    }

    public void setResourcePack(String arg0, byte[] arg1) {
        player.setResourcePack(arg0, arg1);
    }

    public void setResourcePack(String arg0) {
        player.setResourcePack(arg0);
    }

    public void setRotation(float arg0, float arg1) {
        player.setRotation(arg0, arg1);
    }

    public void setSaturation(float arg0) {
        player.setSaturation(arg0);
    }

    public void setScoreboard(Scoreboard arg0) throws IllegalArgumentException, IllegalStateException {
        player.setScoreboard(arg0);
    }

    public void setShoulderEntityLeft(Entity arg0) {
        player.setShoulderEntityLeft(arg0);
    }

    public void setShoulderEntityRight(Entity arg0) {
        player.setShoulderEntityRight(arg0);
    }

    public void setSilent(boolean arg0) {
        player.setSilent(arg0);
    }

    public void setSleepingIgnored(boolean arg0) {
        player.setSleepingIgnored(arg0);
    }

    public void setSneaking(boolean arg0) {
        player.setSneaking(arg0);
    }

    public void setSpectatorTarget(Entity arg0) {
        player.setSpectatorTarget(arg0);
    }

    public void setSprinting(boolean arg0) {
        player.setSprinting(arg0);
    }

    public void setStatistic(Statistic arg0, EntityType arg1, int arg2) {
        player.setStatistic(arg0, arg1, arg2);
    }

    public void setStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
        player.setStatistic(arg0, arg1);
    }

    public void setStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
        player.setStatistic(arg0, arg1, arg2);
    }

    public void setStringValue(String key, String value) {
        player.setStringValue(key, value);
    }

    public void setSwimming(boolean arg0) {
        player.setSwimming(arg0);
    }

    public void setTexturePack(String arg0) {
        player.setTexturePack(arg0);
    }

    public void setTicksLived(int arg0) {
        player.setTicksLived(arg0);
    }

    public void setTotalExperience(int arg0) {
        player.setTotalExperience(arg0);
    }

    public void setVelocity(Vector arg0) {
        player.setVelocity(arg0);
    }

    public void setWalkSpeed(float arg0) throws IllegalArgumentException {
        player.setWalkSpeed(arg0);
    }

    public void setWhitelisted(boolean arg0) {
        player.setWhitelisted(arg0);
    }

    public boolean setWindowProperty(Property arg0, int arg1) {
        return player.setWindowProperty(arg0, arg1);
    }

    public void showPlayer(Player arg0) {
        player.showPlayer(arg0);
    }

    public void showPlayer(Plugin arg0, Player arg1) {
        player.showPlayer(arg0, arg1);
    }

    public boolean sleep(Location arg0, boolean arg1) {
        return player.sleep(arg0, arg1);
    }

    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
                                  double arg6, double arg7, double arg8, T arg9) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
    }

    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
                              double arg7, double arg8) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    }

    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
                                  double arg6, double arg7, T arg8) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    }

    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
                              double arg7) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, T arg5) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4);
    }

    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
                                  double arg6, T arg7) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
                              double arg6) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
                                  T arg6) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5) {
        player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, T arg3) {
        player.spawnParticle(arg0, arg1, arg2, arg3);
    }

    public void spawnParticle(Particle arg0, Location arg1, int arg2) {
        player.spawnParticle(arg0, arg1, arg2);
    }

    public Spigot spigot() {
        return player.spigot();
    }

    public void stopSound(Sound arg0, SoundCategory arg1) {
        player.stopSound(arg0, arg1);
    }

    public void stopSound(Sound arg0) {
        player.stopSound(arg0);
    }

    public void stopSound(String arg0, SoundCategory arg1) {
        player.stopSound(arg0, arg1);
    }

    public void stopSound(String arg0) {
        player.stopSound(arg0);
    }

    public void swingMainHand() {
        player.swingMainHand();
    }

    public void swingOffHand() {
        player.swingOffHand();
    }

    public boolean teleport(Entity arg0, TeleportCause arg1) {
        return player.teleport(arg0, arg1);
    }

    public boolean teleport(Entity arg0) {
        return player.teleport(arg0);
    }

    public boolean teleport(Location arg0, TeleportCause arg1) {
        return player.teleport(arg0, arg1);
    }

    public boolean teleport(Location arg0) {
        return player.teleport(arg0);
    }

    public boolean undiscoverRecipe(NamespacedKey arg0) {
        return player.undiscoverRecipe(arg0);
    }

    public int undiscoverRecipes(Collection<NamespacedKey> arg0) {
        return player.undiscoverRecipes(arg0);
    }

    public void updateCommands() {
        player.updateCommands();
    }

    public void updateInventory() {
        player.updateInventory();
    }

    public void wakeup(boolean arg0) {
        player.wakeup(arg0);
    }

    public boolean checkPlayerNpc() {
        return player.checkPlayerNpc();
    }

    public void copy(InfoMaps infoMaps) {
        player.copy(infoMaps);
    }

    public CoolTime getCoolTime() {
        return player.getCoolTime();
    }

    public String getFileLoc() {
        return player.getFileLoc();
    }

    public NewOfflinePlayer getNewOfflinePlayer() {
        return player.getNewOfflinePlayer();
    }

    public PlayerInfoMaps getPlayerInfoMaps() {
        return player.getPlayerInfoMaps();
    }

    public ScoreBoardHelper getScoreBoardHelper() {
        return player.getScoreBoardHelper();
    }

    public void setScoreBoardHelper(ScoreBoardHelper scoreBoardHelper) {
        player.setScoreBoardHelper(scoreBoardHelper);
    }
}

