import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import AbstractChart from "./AbstractChart.js";

export default class PieChart extends AbstractChart {
  constructor(anchor, radius, hole = 0) {
    super(
      anchor,
      { top: radius, right: radius, bottom: radius, left: radius },
      radius * 2,
      radius * 2
    );

    this.radius = radius;
    this.hole = hole;
  }

  create(data, colors) {
    // Create a color scale
    const color = d3.scaleOrdinal().range(colors);

    // Create the pie generator
    const pie = d3.pie().value((item) => item.value);

    // Create the arc generator
    const arc = d3
      .arc()
      .innerRadius(this.hole) // For a pie chart (0 for no hole, >0 for a donut chart)
      .outerRadius(this.radius);

    // Bind data to pie slices
    this.svg
      .selectAll()
      .data(pie(data))
      .join("path")
      .attr("d", arc)
      .attr("fill", (item) => color(item.data.label))
      .attr("stroke", "white")
      .style("stroke-width", "2px")
      .on("mouseover", this.mouseover)
      .on("mousemove", this.mousemove)
      .on("mouseleave", this.mouseleave);
  }
}
