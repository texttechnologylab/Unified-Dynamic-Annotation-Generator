class PieChart {
  constructor(anchor, radius) {
    this.anchor = anchor;
    this.radius = radius;
  }

  create(data) {
    // Create an SVG container
    const svg = d3
      .select(this.anchor)
      .append("svg")
      .attr("width", this.radius * 2)
      .attr("height", this.radius * 2)
      .append("g")
      .attr("transform", `translate(${this.radius}, ${this.radius})`);

    // Create a color scale
    const color = d3.scaleOrdinal().range(data.map((item) => item.color));

    // Create the pie generator
    const pie = d3.pie().value((item) => item.value);

    // Create the arc generator
    const arc = d3
      .arc()
      .innerRadius(0) // For a pie chart (0 for no hole, >0 for a donut chart)
      .outerRadius(this.radius);

    const tooltip = d3
      .select(this.anchor)
      .append("div")
      .attr("class", "tooltip");

    function mouseover(event) {
      tooltip.style("opacity", 1);
      d3.select(event.currentTarget).style("opacity", 1);
    }

    function mousemove(event, d) {
      tooltip
        .html(`<strong>${d.data.label}</strong><br> ${d.data.value}`)
        .style("left", event.pageX + 20 + "px")
        .style("top", event.pageY + "px");
    }

    function mouseleave(event) {
      tooltip.style("opacity", 0);
      d3.select(event.currentTarget).style("opacity", 0.8);
    }

    // Bind data to pie slices
    svg
      .selectAll("path")
      .data(pie(data))
      .enter()
      .append("path")
      .attr("d", arc)
      .attr("fill", (item) => color(item.data.label))
      .attr("stroke", "white")
      .style("stroke-width", "2px")
      .style("opacity", 0.8)
      .on("mouseover", mouseover)
      .on("mousemove", mousemove)
      .on("mouseleave", mouseleave);

    // Add labels to slices
    // svg
    //   .selectAll("text")
    //   .data(pie(data))
    //   .enter()
    //   .append("text")
    //   .attr("transform", (item) => `translate(${arc.centroid(item)})`)
    //   .text((item) => item.data.label)
    //   .style("font-size", "24px")
    //   .style("fill", "white");
  }
}
