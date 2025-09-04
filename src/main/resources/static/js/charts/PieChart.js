import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";
import ControlsHandler from "../utils/ControlsHandler.js";
import ExportHandler from "../utils/ExportHandler.js";
import { maxOf, minOf } from "../utils/helper.js";

export default class PieChart extends D3Visualization {
  constructor(root, endpoint, { radius, hole = 0 }) {
    super(
      root,
      endpoint,
      { top: radius, right: radius, bottom: radius, left: radius },
      radius * 2,
      radius * 2
    );
    this.controls = new ControlsHandler(this.root.select(".dv-sidepanel-body"));
    this.exports = new ExportHandler(this.root.select(".dv-dropdown"), [
      "svg",
      "png",
      "csv",
      "json",
    ]);

    this.radius = radius;
    this.hole = hole;
  }

  init(data) {
    const min = minOf(data.map((d) => d.value));
    const max = maxOf(data.map((d) => d.value));

    // Add controls
    this.controls.appendSelectRadio(
      "Sort by",
      ["value", "label"],
      ["desc", "asc"],
      (sort, order) => {
        this.filter.sort = sort;
        this.filter.desc = order === "desc";
        this.fetch().then((data) => this.render(data));
      }
    );

    this.controls.appendInputRadio(
      "Filter labels by",
      ["includes", "regex"],
      (input, type) => {
        this.filter.filter = input;
        this.filter.regex = type === "regex";
        this.fetch().then((data) => this.render(data));
      }
    );

    this.controls.appendDoubleSlider(min, max, (min, max) => {
      this.filter.min = min;
      this.filter.max = max;
      this.fetch().then((data) => this.render(data));
    });
  }

  async render(data) {
    this.clear();

    if (!data) {
      data = await this.fetch();
      this.init(data);
    }

    // Create a color scale
    const color = d3.scaleOrdinal().range(data.map((item) => item.color));

    // Create the pie generator
    const pie = d3.pie().value((item) => item.value);

    // Create the arc generator
    const arc = d3
      .arc()
      .innerRadius(this.hole) // For a pie chart (0 for no hole, >0 for a donut chart)
      .outerRadius(this.radius);

    // Bind data to pie slices
    this.svg
      .select("g")
      .selectAll()
      .data(pie(data))
      .join("path")
      .attr("d", arc)
      .attr("fill", color)
      .attr("stroke", "white")
      .style("stroke-width", "2px")
      .on("mouseover", (event) => this.mouseover(event.currentTarget))
      .on("mousemove", (event, { data }) =>
        this.mousemove(
          event.pageY,
          event.pageX + 20,
          `<strong>${data.label}</strong><br>${data.value}`
        )
      )
      .on("mouseleave", (event) => this.mouseleave(event.currentTarget));

    // Pass data to export handler
    this.exports.update(data, this.svg.node());
  }
}
