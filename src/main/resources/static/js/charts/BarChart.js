import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";

export default class BarChart extends D3Visualization {
  constructor(anchor, endpoint, { width, height, horizontal = false }) {
    super(
      anchor,
      endpoint,
      { top: 30, right: 30, bottom: 70, left: 60 },
      width,
      height
    );

    this.horizontal = horizontal;
  }

  render() {
    this.fetch().then((data) => {
      data = data.sort((a, b) => b.value - a.value);

      // Add x axis
      const xAxis = this.horizontal ? this.linear(data) : this.band(data);
      this.svg
        .append("g")
        .attr("transform", `translate(0, ${this.height})`)
        .call(d3.axisBottom(xAxis))
        .selectAll("text")
        .attr("transform", "translate(-10,0)rotate(-45)")
        .style("text-anchor", "end");

      // Add y axis
      const yAxis = this.horizontal ? this.band(data) : this.linear(data);
      this.svg.append("g").call(d3.axisLeft(yAxis));

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
        .selectAll()
        .data(data)
        .join("rect")
        .attr("x", x)
        .attr("y", y)
        .attr("width", width)
        .attr("height", height)
        .attr("fill", (item) => item.color)
        .on("mouseover", this.mouseover)
        .on("mousemove", this.mousemove)
        .on("mouseleave", this.mouseleave);
    });
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
