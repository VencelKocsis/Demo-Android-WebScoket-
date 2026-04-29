package hu.bme.aut.android.demo.domain.market.repository

import hu.bme.aut.android.demo.domain.market.model.MarketItem

/**
 * A Piac (Market) funkció műveleteinek szerződése (interfésze) a Domain rétegben.
 * * Definiálja, hogy milyen piaci funkciók érhetők el az alkalmazásban anélkül,
 * hogy tudná, a háttérben milyen API hívások (pl. Retrofit) valósítják meg azokat.
 */
interface MarketRepository {

    /**
     * Lekérdezi az összes olyan felszerelést a backendről, amelyet a tulajdonosuk "eladó" (isForSale = true)
     * státuszba állított.
     *
     * @return Az eladó felszerelések és tulajdonosaik listája.
     */
    suspend fun getMarketItems(): List<MarketItem>

    /**
     * Elküld egy érdeklődést a backendnek, amely ezután push értesítést (FCM)
     * küld a felszerelés tulajdonosának.
     *
     * @param equipmentId Annak a felszerelésnek az azonosítója, amely iránt a felhasználó érdeklődik.
     */
    suspend fun inquireAboutEquipment(equipmentId: Int)
}