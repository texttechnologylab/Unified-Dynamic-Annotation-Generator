import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";
import { appendSlider } from "../utils/controls.js";
import ExportHandler from "../utils/ExportHandler.js";

export default class PieChart extends D3Visualization {
  constructor(root, endpoint, { radius, hole = 0 }) {
    super(
      root,
      endpoint,
      { top: radius, right: radius, bottom: radius, left: radius },
      radius * 2,
      radius * 2
    );
    this.handler = new ExportHandler(this.root.select(".dv-dropdown"), [
      "svg",
      "png",
      "csv",
      "json",
    ]);

    this.radius = radius;
    this.hole = hole;
  }

  async render(data) {
    this.clear();

    if (!data) {
      data = await this.fetch();

      // Add controls
      const min = data[data.length - 1].value;
      const max = data[0].value;

      appendSlider(this.controls, min, max, (min, max) => {
        this.filter.min = min;
        this.filter.max = max;
        this.fetch().then((data) => this.render(data));
      });
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
    this.handler.update(data, this.svg.node());
  }
}
