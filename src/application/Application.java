package application;

import static com.anotherbrick.inthewall.Config.MyColorEnum.WHITE;

import java.util.ArrayList;
import java.util.Random;

import processing.core.PVector;

import com.anotherbrick.inthewall.Config.MyColorEnum;
import com.anotherbrick.inthewall.FilterToolbox;
import com.anotherbrick.inthewall.Legend;
import com.anotherbrick.inthewall.NotificationCenter;
import com.anotherbrick.inthewall.PlotData;
import com.anotherbrick.inthewall.StateInfo;
import com.anotherbrick.inthewall.TouchEnabled;
import com.anotherbrick.inthewall.VizButton;
import com.anotherbrick.inthewall.VizGraph;
import com.anotherbrick.inthewall.VizMap;
import com.anotherbrick.inthewall.VizPanel;
import com.anotherbrick.inthewall.VizScatterPlot;
import com.anotherbrick.inthewall.VizTimeSlider;
import com.modestmaps.geo.Location;

public class Application extends VizPanel implements TouchEnabled {

    private VizMap map;
    private final float MAP_WIDTH = 537;
    private final float MAP_HEIGHT = 384;
    private final float MAP_X0 = 0;
    private final float MAP_Y0 = 0;

    private VizGraph graph;
    private final float GRAPH_WIDTH = 436;
    private final float GRAPH_HEIGHT = 270;
    private final float GRAPH_X0 = 861;
    private final float GRAPH_Y0 = 21;

    private VizScatterPlot sp;
    private final float SP_WIDTH = GRAPH_WIDTH;
    private final float SP_HEIGHT = 250;
    private final float SP_X0 = GRAPH_X0;
    private final float SP_Y0 = GRAPH_Y0;

    private VizTimeSlider timeslider;
    private final float SLIDER_WIDTH = 396;
    private final float SLIDER_HEIGHT = 25;
    private final float SLIDER_X0 = 861;
    private final float SLIDER_Y0 = 295;

    private FilterToolbox ft;
    private final float FT_WIDTH = 281;
    private final float FT_HEIGHT = 363;
    private final float FT_X0 = 560;
    private final float FT_Y0 = 18;

    private VizButton tabButton;
    private final float BUTTON_HEIGHT = 40;
    private final float BUTTON_WIDTH = 60;
    private final float BUTTON_X0 = GRAPH_X0;
    private final float BUTTON_Y0 = SLIDER_Y0 + SLIDER_HEIGHT + 5;

    private Legend legend;
    private final float LEGEND_X0 = BUTTON_X0 + BUTTON_WIDTH + 5;
    private final float LEGEND_Y0 = BUTTON_Y0;
    private final float LEGEND_W = GRAPH_WIDTH - (BUTTON_WIDTH + 5);
    private final float LEGEND_H = 40;

    private VizButton historyButton;
    private final float HISTORY_H = 25;
    private final float HISTORY_W = 60;
    private final float HISTORY_X0 = SLIDER_WIDTH + SLIDER_X0 + 5;
    private final float HISTORY_Y0 = SLIDER_Y0;

    private enum Mode {
	GRAPH, SCATTER
    }

    private Mode currentMode = Mode.GRAPH;

    public Application(float x0, float y0, float width, float height) {
	super(x0, y0, width, height);
    }

    @Override
    public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
	if (down) {
	    if (tabButton.containsPoint(x, y)) {
		currentMode = currentMode == Mode.GRAPH ? Mode.SCATTER
			: Mode.GRAPH;
	    } else if (historyButton.containsPoint(x, y)) {
		graph.showHistoricalEvents = graph.showHistoricalEvents ? false
			: true;
	    }
	}
	propagateTouch(x, y, down, touchType);
	return false;
    }

    @Override
    public void setup() {

	legend = new Legend(LEGEND_X0, LEGEND_Y0, LEGEND_W, LEGEND_H, this);

	historyButton = new VizButton(HISTORY_X0, HISTORY_Y0, HISTORY_W,
		HISTORY_H, this);
	historyButton.setStyle(MyColorEnum.MEDIUM_GRAY, WHITE,
		MyColorEnum.DARK_WHITE, 255, 255, 12);
	historyButton.setRoundedCornerd(5, 5, 5, 5);
	historyButton.setText("Events");

	map = new VizMap(MAP_X0, MAP_Y0, MAP_WIDTH, MAP_HEIGHT, this);
	map.setup();
	addTouchSubscriber(map);

	ft = new FilterToolbox(FT_X0, FT_Y0, FT_WIDTH, FT_HEIGHT, this);
	ft.setup();
	addTouchSubscriber(ft);

	graph = new VizGraph(GRAPH_X0, GRAPH_Y0, GRAPH_WIDTH, GRAPH_HEIGHT,
		this);
	graph.setup();
	addTouchSubscriber(graph);

	sp = new VizScatterPlot(SP_X0, SP_Y0, SP_WIDTH, SP_HEIGHT, this);
	sp.setup();
	addTouchSubscriber(sp);

	timeslider = new VizTimeSlider(SLIDER_X0, SLIDER_Y0, SLIDER_WIDTH,
		SLIDER_HEIGHT, this, graph);
	timeslider.setup();
	addTouchSubscriber(timeslider);

	StateInfo si = new StateInfo(17, "Illinois", new Location(40.633125f,
		-89.398528f), 6);
	NotificationCenter.getInstance().notifyEvent("state-changed", si);
	NotificationCenter.getInstance().notifyEvent("year-changed", 2001);
	NotificationCenter.getInstance().notifyEvent(
		"update-graph",
		DBUtil.getInstance().getCounts(new FilterWrapper("crashid"),
			si.getId()));

	tabButton = new VizButton(BUTTON_X0, BUTTON_Y0, BUTTON_WIDTH,
		BUTTON_HEIGHT, this);
	tabButton.setStyle(MyColorEnum.MEDIUM_GRAY, WHITE,
		MyColorEnum.DARK_WHITE, 255, 255, 12);
	tabButton.setRoundedCornerd(5, 5, 5, 5);
	tabButton.setText("Graph /\n Scatter");
    }

    @Override
    public boolean draw() {
	pushStyle();
	background(MyColorEnum.DARK_GRAY);

	map.draw();
	coverExceedingTiles();
	switch (currentMode) {
	case GRAPH:
	    graph.setVisible(true);
	    sp.setVisible(false);
	    graph.draw();
	    break;
	case SCATTER:
	    sp.setVisible(true);
	    graph.setVisible(false);
	    sp.draw();
	    break;
	default:
	    break;
	}

	ft.draw();
	timeslider.draw();
	tabButton.draw();
	tabButton.drawTextCentered();
	legend.draw();
	historyButton.draw();
	historyButton.drawTextCentered();

	popStyle();
	return false;
    }

    private void coverExceedingTiles() {
	pushStyle();
	noStroke();
	fill(MyColorEnum.DARK_GRAY);
	rect(MAP_X0 + MAP_WIDTH, 0, getWidth() - MAP_WIDTH, getHeight());
	popStyle();
    }

    private void addDummyPlots() {
	Random generator = new Random();
	ArrayList<PVector> points = new ArrayList<PVector>();

	for (int i = 2001; i < 2011; i++) {
	    points.add(new PVector(i, 10 * generator.nextFloat()));
	}

	PlotData plot = new PlotData(points, MyColorEnum.RED);
	plot.setFilled(true);
	graph.addPlot(plot, 0);
	timeslider.addPlot(plot, 0);

	sp.setDots(points);
    }
}
