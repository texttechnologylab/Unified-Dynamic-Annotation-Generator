import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";

export default class LineChart extends D3Visualization {
  constructor(anchor, width, height, line = true, dots = true) {
    super(anchor, { top: 10, right: 30, bottom: 30, left: 60 }, width, height);

    this.line = line;
    this.dots = dots;
  }

  create(data, color) {
    // Add x axis
    const xAxis = d3
      .scaleLinear()
      .range([0, this.width])
      .domain([0, d3.max(data, (item) => item.x)]);
    this.svg
      .append("g")
      .attr("transform", `translate(0, ${this.height})`)
      .call(d3.axisBottom(xAxis));

    // Add y axis
    const yAxis = d3
      .scaleLinear()
      .range([this.height, 0])
      .domain([0, d3.max(data, (item) => item.y)]);
    this.svg.append("g").call(d3.axisLeft(yAxis));

    const line = d3
      .line()
      .x((item) => xAxis(item.x))
      .y((item) => yAxis(item.y));

    // Add the line
    if (this.line) {
      this.svg
        .datum(data)
        .append("path")
        .attr("d", line)
        .attr("fill", "none")
        .attr("stroke", color)
        .attr("stroke-width", 1.5);
    }

    // Add the dots
    this.svg
      .selectAll("circle")
      .data(data)
      .join("circle")
      .attr("cx", (item) => xAxis(item.x))
      .attr("cy", (item) => yAxis(item.y))
      .attr("r", 4)
      .attr("fill", this.dots ? color : "transparent")
      .on("mouseover", this.mouseover)
      .on("mousemove", this.mousemove)
      .on("mouseleave", this.mouseleave);
  }

  mousemove = (event, data) => {
    this.tooltip
      .html(`(${data.x}, ${data.y})`)
      .style("left", event.pageX + 20 + "px")
      .style("top", event.pageY + "px");
  };
}
