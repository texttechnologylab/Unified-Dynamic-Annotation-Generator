import BarChartHandler from "./handler/widgets/charts/BarChartHandler.js";
import LineChartHandler from "./handler/widgets/charts/LineChartHandler.js";
import PieChartHandler from "./handler/widgets/charts/PieChartHandler.js";
import Map2DHandler from "./handler/widgets/maps/Map2DHandler.js";
import Network2DHandler from "./handler/widgets/networks/Network2DHandler.js";
import StaticImageHandler from "./handler/widgets/static/StaticImageHandler.js";
import StaticTextHandler from "./handler/widgets/static/StaticTextHandler.js";
import HighlightTextHandler from "./handler/widgets/text/HighlightTextHandler.js";

export default {
  BarChart: BarChartHandler,
  LineChart: LineChartHandler,
  PieChart: PieChartHandler,
  Map2D: Map2DHandler,
  Network2D: Network2DHandler,
  StaticImage: StaticImageHandler,
  StaticText: StaticTextHandler,
  HighlightText: HighlightTextHandler,
};
