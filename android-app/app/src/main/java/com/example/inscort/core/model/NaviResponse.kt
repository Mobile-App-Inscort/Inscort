package com.example.inscort.core.model
// 전체 응답
data class NaviResponse(
    val routes: List<Route>
)

data class Route(
    val sections: List<Section>
)

data class Section(
    val roads: List<Road>
)

data class Road(
    val name: String,
    val vertexes: List<Double> // [x, y, x, y...] 좌표 덩어리
)