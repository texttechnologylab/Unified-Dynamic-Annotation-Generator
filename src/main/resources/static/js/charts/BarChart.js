import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";
import { appendSlider } from "../utils/controls.js";
import ExportHandler from "../utils/ExportHandler.js";

export default class BarChart extends D3Visualization {
  constructor(root, endpoint, { width, height, horizontal = false }) {
    super(
      root,
      endpoint,
      { top: 30, right: 30, bottom: 70, left: 60 },
      width,
      height
    );
    this.handler = new ExportHandler(this.root.select(".dv-dropdown"), [
      "svg",
      "png",
      "csv",
      "json",
    ]);

    this.horizontal = horizontal;
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

    // Add x axis
    const xAxis = this.horizontal ? this.linear(data) : this.band(data);
    this.svg
      .select("g")
      .append("g")
      .attr("transform", `translate(0, ${this.height})`)
      .call(d3.axisBottom(xAxis))
      .selectAll("text")
      .attr("transform", "translate(-10,0)rotate(-45)")
      .style("text-anchor", "end");

    // Add y axis
    const yAxis = this.horizontal ? this.band(data) : this.linear(data);
    this.svg.select("g").append("g").call(d3.axisLeft(yAxis));

    const x = this.horizontal ? xAxis(0) : (item) => xAxis(item.label);
    const y = this.horizontal
      ? (item) => yAxis(item.label)
      : (item) => yAxis(item.value);
    const width = this.horizontal
      ? (item) => xAxis(item.value)
      : xAxis.bandwidth();
    const height = this.horizontal
      ? yAxis.bandwidth()
      : (item) => this.height - yAxis(item.value);

    // Add the bars
    this.svg
      .select("g")
      .selectAll()
      .data(data)
      .join("rect")
      .attr("x", x)
      .attr("y", y)
      .attr("width", width)
      .attr("height", height)
      .attr("fill", (item) => item.color)
      .on("mouseover", (event) => this.mouseover(event.currentTarget))
      .on("mousemove", (event, data) =>
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

  band(data) {
    return d3
      .scaleBand()
      .range(this.horizontal ? [0, this.height] : [0, this.width])
      .domain(data.map((item) => item.label))
      .padding(0.2);
  }

  linear(data) {
    return d3
      .scaleLinear()
      .range(this.horizontal ? [0, this.width] : [this.height, 0])
      .domain([0, data[0].value]);
  }
}
