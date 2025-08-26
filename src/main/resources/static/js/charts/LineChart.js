import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";
import { flatData } from "../utils/helper.js";
import { appendSwitch } from "../utils/controls.js";

export default class LineChart extends D3Visualization {
  constructor(root, endpoint, { width, height, line = true, dots = true }) {
    super(
      root,
      endpoint,
      { top: 10, right: 30, bottom: 30, left: 60 },
      width,
      height
    );

    this.line = line;
    this.dots = dots;
  }

  async render(data) {
    this.clear();

    if (!data) {
      data = await this.fetch();

      // Add controls
      for (const item of data) {
        appendSwitch(this.controls, item.name, (value) => console.log(value));
      }
    }

    const coordinates = flatData(data, "coordinates");

    // Add x axis
    const xAxis = d3
      .scaleLinear()
      .range([0, this.width])
      .domain(d3.extent(coordinates, (item) => item.x));
    this.svg
      .append("g")
      .attr("transform", `translate(0, ${this.height})`)
      .call(d3.axisBottom(xAxis));

    // Add y axis
    const yAxis = d3
      .scaleLinear()
      .range([this.height, 0])
      .domain(d3.extent(coordinates, (item) => item.y));
    this.svg.append("g").call(d3.axisLeft(yAxis));

    const line = d3
      .line()
      .x((item) => xAxis(item.x))
      .y((item) => yAxis(item.y));

    // Add the line
    if (this.line) {
      this.svg
        .selectAll(".line")
        .data(data)
        .join("path")
        .attr("d", (item) => line(item.coordinates))
        .attr("fill", "none")
        .attr("stroke", (item) => item.color)
        .attr("stroke-width", 2.5);
    }

    // Add the dots
    this.svg
      .selectAll(".circle")
      .data(coordinates)
      .join("circle")
      .attr("cx", (item) => xAxis(item.x))
      .attr("cy", (item) => yAxis(item.y))
      .attr("r", 4)
      .attr("fill", this.dots ? (item) => item.color : "transparent")
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
