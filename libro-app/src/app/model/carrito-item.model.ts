import { Libro } from "./libro.model";

export interface CarritoItem{
    idCaiitoItem? : number;
    libro: Libro;
    cantidad: number;
    precioUnitario: number;
    total: number;
}