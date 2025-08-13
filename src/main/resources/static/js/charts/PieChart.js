import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";

export default class PieChart extends D3Visualization {
  constructor(anchor, endpoint, { radius, hole = 0 }) {
    super(
      anchor,
      endpoint,
      { top: radius, right: radius, bottom: radius, left: radius },
      radius * 2,
      radius * 2
    );

    this.radius = radius;
    this.hole = hole;
  }

  render() {
    this.fetch().then((data) => {
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
        .selectAll()
        .data(pie(data))
        .join("path")
        .attr("d", arc)
        .attr("fill", color)
        .attr("stroke", "white")
        .style("stroke-width", "2px")
        .on("mouseover", this.mouseover)
        .on("mousemove", this.mousemove)
        .on("mouseleave", this.mouseleave);
    });
  }
}
