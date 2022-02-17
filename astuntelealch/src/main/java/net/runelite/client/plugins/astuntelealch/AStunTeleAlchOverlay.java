
package net.runelite.client.plugins.astuntelealch;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.TitleComponent;
import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;
//import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Slf4j
@Singleton
class AStunTeleAlchOverlay extends OverlayPanel
{
    private final Client client;
    private final AStunTeleAlchPlugin plugin;
    private final AStunTeleAlchConfig config;

    String timeFormat;
    String localstate = "Starting";
    private String infoStatus = "Starting...";

    @Inject
    private AStunTeleAlchOverlay(final Client client, final AStunTeleAlchPlugin plugin, final AStunTeleAlchConfig config)
    {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Overlay"));
        setPriority(OverlayPriority.HIGHEST);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.botTimer == null || !plugin.startFireMaker || !config.enableUI())
        {
            log.debug("Overlay conditions not met, not starting overlay");
            return null;
        }
        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
        Duration duration = Duration.between(plugin.botTimer, Instant.now());
        timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
       tableComponent.addRow("Time:", formatDuration(duration.toMillis(), timeFormat));
        tableComponent.addRow("State:", plugin.state.toString());
        //tableComponent.addRow("Path: ", String.valueOf(plugin.firemakingPath));
        if (!tableComponent.isEmpty())
        {
            panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212"));
            panelComponent.setPreferredSize(new Dimension(200, 200));
            panelComponent.setBorder(new Rectangle(5, 5, 5, 5));
            panelComponent.getChildren().add(TitleComponent.builder().text("Anarchise Stun/Tele Alcher").color(ColorUtil.fromHex("#40C4FF")).build());
            panelComponent.getChildren().add(tableComponent);
        }
        renderFires(graphics);
        return super.render(graphics);
    }
    private void renderFires(Graphics2D graphics)
    {
        for(GameObject fire : plugin.fireObjects) {
            if(fire.getCanvasTilePoly()!=null){
                OverlayUtil.renderPolygon(graphics, fire.getCanvasTilePoly(), Color.RED);
            }
        }
    }
}
