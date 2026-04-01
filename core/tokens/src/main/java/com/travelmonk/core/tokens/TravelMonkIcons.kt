package com.travelmonk.core.tokens

object TravelMonkIcons {
    val Home = R.drawable.ic_home
    val Flight = R.drawable.ic_flight
    val Hotel = R.drawable.ic_hotel
    val Explore = R.drawable.ic_explore
    val ConfirmationNumber = R.drawable.ic_confirmation_number
    val CheckCircle = R.drawable.ic_check_circle
    val Notifications = R.drawable.ic_notifications
    val Search = R.drawable.ic_search
    val SelfImprovement = R.drawable.ic_self_improvement
    val CardGiftCard = R.drawable.ic_card_giftcard
    val ArrowBack = R.drawable.ic_arrow_back
    val FlightTakeoff = R.drawable.ic_flight_takeoff
    val FlightLand = R.drawable.ic_flight_land
    val CalendarToday = R.drawable.ic_calendar_today
    val Person = R.drawable.ic_person
    val CleaningServices = R.drawable.ic_cleaning_services
    val Inventory = R.drawable.ic_inventory
    val Star = R.drawable.ic_star
    val Apartment = R.drawable.ic_apartment
    val HolidayVillage = R.drawable.ic_holiday_village
    val LocationOn = R.drawable.ic_location_on
    val RealEstateAgent = R.drawable.ic_real_estate_agent
    val PersonSearch = R.drawable.ic_person_search
    val SupportAgent = R.drawable.ic_support_agent
    val LocalLaundryService = R.drawable.ic_local_laundry_service
    val DirectionsCar = R.drawable.ic_directions_car
    val DirectionsTransit = R.drawable.ic_directions_transit

    val Swap_Vert = R.drawable.ic_swap_vert

    fun byName(name: String): Int = when (name) {
        "home" -> Home
        "flight" -> Flight
        "hotel" -> Hotel
        "explore" -> Explore
        "confirmation_number" -> ConfirmationNumber
        "check_circle" -> CheckCircle
        "notifications" -> Notifications
        "search" -> Search
        "self_improvement" -> SelfImprovement
        "card_giftcard" -> CardGiftCard
        "arrow_back" -> ArrowBack
        "flight_takeoff" -> FlightTakeoff
        "flight_land" -> FlightLand
        "calendar_today" -> CalendarToday
        "person" -> Person
        "cleaning_services" -> CleaningServices
        "inventory" -> Inventory
        "star" -> Star
        "apartment" -> Apartment
        "holiday_village" -> HolidayVillage
        "location_on" -> LocationOn
        "real_estate_agent" -> RealEstateAgent
        "person_search" -> PersonSearch
        "support_agent" -> SupportAgent
        "local_laundry_service" -> LocalLaundryService
        "directions_car" -> DirectionsCar
        "directions_transit" -> DirectionsTransit
        "swap_vert" -> Swap_Vert
        else -> Search
    }
}
