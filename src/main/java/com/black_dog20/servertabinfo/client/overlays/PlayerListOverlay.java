package com.black_dog20.servertabinfo.client.overlays;

import com.black_dog20.bml.client.DrawingContext;
import com.black_dog20.bml.client.overlay.GameOverlay;
import com.black_dog20.bml.client.rows.Row;
import com.black_dog20.bml.client.rows.RowHelper;
import com.black_dog20.bml.client.rows.columns.BlankColumn;
import com.black_dog20.bml.client.rows.columns.Column;
import com.black_dog20.bml.client.rows.columns.HeadColumn;
import com.black_dog20.bml.client.rows.columns.ITextComponentColumn;
import com.black_dog20.bml.utils.dimension.DimensionUtil;
import com.black_dog20.bml.utils.text.TextComponentBuilder;
import com.black_dog20.servertabinfo.Config;
import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.client.ClientDataManager;
import com.black_dog20.servertabinfo.client.keybinds.Keybinds;
import com.black_dog20.servertabinfo.common.utils.Dimension;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.overlay.NamedGuiOverlay;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.black_dog20.servertabinfo.common.utils.Translations.*;
import static net.minecraft.world.scores.DisplaySlot.LIST;

@OnlyIn(Dist.CLIENT)
public class PlayerListOverlay extends GameOverlay.Pre {

    private static final Ordering<PlayerInfo> ENTRY_ORDERING = Ordering.from(new PlayerComparator());
    private final Minecraft minecraft;
    private final Font fontRenderer;
    private long lastRenderTime = Util.getMillis();
    private int ticks = 0;
    private int page = 1;

    public PlayerListOverlay() {
        this.minecraft = Minecraft.getInstance();
        this.fontRenderer = minecraft.font;
    }

    @Override
    public void onRender(GuiGraphics guiGraphics, int width, int height) {
        if(Keybinds.SHOW.isDown())
            return;
        int y = 10;
        int z = 0;
        if (Util.getMillis() - 2000 > lastRenderTime) {
            page = 1;
            ticks = 1;
        }
        int itemsPerPage = (int) Math.floor((height - 7 * y) / fontRenderer.lineHeight);
        List<Row> rows = getRows();

        int maxPages = (int) Math.ceil(rows.size() / (double) itemsPerPage);

        if (ticks % 300 == 0) {
            if (page >= maxPages)
                page = 1;
            else
                page++;
            ticks = 1;
        }

        rows = getPagedRows(rows, itemsPerPage);
        int maxWidth = RowHelper.getMaxWidth(rows);
        int x = width / 2 - maxWidth / 2;

        DrawingContext drawingContext = new DrawingContext(guiGraphics, width, height, x, y, z, fontRenderer);
        y = RowHelper.drawRowsWithBackground(drawingContext, rows);
        guiGraphics.drawString(fontRenderer, PAGE.get(page, maxPages), width / 2 + 2, y + 2, -1);
        ticks++;
        lastRenderTime = Util.getMillis();
    }

    @Override
    public boolean doRender(NamedGuiOverlay overlay) {
        if (overlay.id().equals(VanillaGuiOverlay.PLAYER_LIST.id())) {
            Objective scoreboard = minecraft.level.getScoreboard().getDisplayObjective(LIST);
            ClientPacketListener handler = minecraft.player.connection;
            boolean shouldShowTabList = (minecraft.options.keyPlayerList.isDown() && (!minecraft.isLocalServer() || handler.getOnlinePlayers().size() > 1 || scoreboard != null));
            return shouldShowTabList && Config.REPLACE_PLAYER_LIST.get();
        }

        return false;
    }

    @Override
    public boolean doesCancelEvent() {
        return true;
    }

    private List<Row> getRows() {
        ClientPacketListener nethandlerplayclient = this.minecraft.player.connection;
        List<PlayerInfo> list = ENTRY_ORDERING.<PlayerInfo>sortedCopy(nethandlerplayclient.getListedOnlinePlayers());

        return list.stream()
                .map(this::buildRow)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Row buildRow(PlayerInfo playerInfo) {
        Row.RowBuilder builder = new Row.RowBuilder();
        Scoreboard scoreboard = Optional.ofNullable(minecraft.level)
                .map(ClientLevel::getScoreboard)
                .orElse(null);

        return builder
                .withColumn(HeadColumn.of("head", playerInfo))
                .withColumn(ITextComponentColumn.of("name", getPlayerName(playerInfo)))
                .withColumn(BlankColumn.of("nameSpace", 6))
                .withColumn(ITextComponentColumn.of("dim", getPlayerDim(playerInfo)))
                .withColumn(BlankColumn.of("dimSpace", 3))
                .withColumn(ITextComponentColumn.of("score", getPlayerScore(playerInfo, scoreboard)), showScoreObjective(scoreboard))
                .withColumn(BlankColumn.of("scoreSpace", 3), showScoreObjective(scoreboard))
                .withColumn(ITextComponentColumn.of("ping", getPlayerPing(playerInfo), Column.Alignment.RIGHT))
                .withColumn(BlankColumn.of("pingSpace", 1))
                .build();
    }

    private Component getPlayerScore(PlayerInfo playerInfo, Scoreboard scoreboard) {
        Objective scoreObjective = Optional.ofNullable(scoreboard)
                .map(s -> s.getDisplayObjective(LIST))
                .orElse(null);

        return TextComponentBuilder.of("")
                .with(getScore(playerInfo, scoreboard, scoreObjective), showPlayerScore(playerInfo, scoreObjective))
                .format(ChatFormatting.YELLOW)
                .build();
    }

    private Supplier<Boolean> showPlayerScore(PlayerInfo playerInfo, Objective scoreObjective) {
        return () -> scoreObjective != null && GameType.SPECTATOR != playerInfo.getGameMode();
    }

    private int getScore(PlayerInfo playerInfo, Scoreboard scoreboard, Objective scoreObjective) {
        if (scoreObjective != null) {
            ScoreHolder scoreholder = ScoreHolder.fromGameProfile(playerInfo.getProfile());
            ScoreAccess score = scoreboard.getOrCreatePlayerScore(scoreholder, scoreObjective);
            return score.get();
        } else {
            return 0;
        }
    }

    private Supplier<Boolean> showScoreObjective(Scoreboard scoreboard) {
        return () -> scoreboard != null && scoreboard.getDisplayObjective(LIST) != null;
    }

    private boolean isAllowed() {
        return !Config.OP_ONLY_MODE.get() || minecraft.hasSingleplayerServer() || Optional.ofNullable(minecraft.player)
                .map(player -> player.hasPermissions(1))
                .orElse(false);
    }

    private List<Row> getPagedRows(List<Row> rows, int itemsPerPage) {
        rows = rows.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .collect(Collectors.toCollection(LinkedList::new));
        return rows;
    }

    private Component getPlayerName(PlayerInfo playerInfo) {
        return Optional.ofNullable(playerInfo)
                .map(PlayerInfo::getTabListDisplayName)
                .orElseGet(() -> PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(playerInfo.getProfile().getName())));
    }

    private Component getPlayerPing(PlayerInfo playerInfo) {
        return TextComponentBuilder.of(playerInfo.getLatency())
                .with(MS)
                .build();
    }

    private Component getPlayerDim(PlayerInfo playerInfo) {
        ResourceLocation dimName = ClientDataManager.PLAYER_DIMENSIONS.getOrDefault(playerInfo.getProfile().getId(), null);
        return TextComponentBuilder.of(getDimensionName(dimName))
                .with(getDimensionTps(dimName), this::isAllowed)
                .build();

    }

    private Component getDimensionName(ResourceLocation dimensionName) {
        if (dimensionName == null)
            return UNKOWN.get();

        if (ClientDataManager.DIMENSION_NAME_CACHE.containsKey(dimensionName))
            return ClientDataManager.DIMENSION_NAME_CACHE.get(dimensionName);

        MutableComponent name = DimensionUtil.getFormattedDimensionName(dimensionName, ServerTabInfo.MOD_ID);
        ClientDataManager.DIMENSION_NAME_CACHE.put(dimensionName, name);
        return name;
    }

    private Component getDimensionTps(ResourceLocation dimensionName) {
        if (dimensionName == null)
            return Component.literal("");
        Dimension dimension = ClientDataManager.DIMENSIONS.stream()
                .filter(d -> d.name.equals(dimensionName))
                .findFirst()
                .orElse(null);
        if (dimension == null)
            return Component.literal("");

        int tps = dimension.tps;
        ChatFormatting color = ChatFormatting.GREEN;

        if (tps < 20)
            color = ChatFormatting.YELLOW;
        if (tps <= 10)
            color = ChatFormatting.RED;

        return TextComponentBuilder.of("(")
                .with(tps)
                .format(color)
                .with(")")
                .build();
    }

    @OnlyIn(Dist.CLIENT)
    static class PlayerComparator implements Comparator<PlayerInfo> {

        public int compare(PlayerInfo player1, PlayerInfo player2) {
            PlayerTeam scoreplayerteam1 = player1.getTeam();
            PlayerTeam scoreplayerteam2 = player2.getTeam();
            return ComparisonChain.start()
                    .compareTrueFirst(player1.getGameMode() != GameType.SPECTATOR, player2.getGameMode() != GameType.SPECTATOR)
                    .compare(getName(scoreplayerteam1), getName(scoreplayerteam2))
                    .compare(player1.getProfile().getName(), player2.getProfile().getName())
                    .result();
        }

        private String getName(PlayerTeam scorePlayerTeam) {
            return Optional.ofNullable(scorePlayerTeam)
                    .map(PlayerTeam::getName)
                    .orElse("");
        }
    }
}
