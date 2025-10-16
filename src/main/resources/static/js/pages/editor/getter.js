import BarChartHandler from "./handler/widgets/charts/BarChartHandler.js";
import LineChartHandler from "./handler/widgets/charts/LineChartHandler.js";
import PieChartHandler from "./handler/widgets/charts/PieChartHandler.js";
import Map2DHandler from "./handler/widgets/maps/Map2DHandler.js";
import Network2DHandler from "./handler/widgets/networks/Network2DHandler.js";
import StaticImageHandler from "./handler/widgets/static/StaticImageHandler.js";
import StaticTextHandler from "./handler/widgets/static/StaticTextHandler.js";
import HighlightTextHandler from "./handler/widgets/text/HighlightTextHandler.js";

export default {
  StaticText: StaticTextHandler,
  StaticImage: StaticImageHandler,
  BarChart: BarChartHandler,
  PieChart: PieChartHandler,
  LineChart: LineChartHandler,
  HighlightText: HighlightTextHandler,
  Network2D: Network2DHandler,
  Map2D: Map2DHandler,
};
