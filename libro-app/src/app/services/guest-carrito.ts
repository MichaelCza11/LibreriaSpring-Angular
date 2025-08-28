import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { getCartToken } from '../core/cart-token';
import { Observable } from 'rxjs';
import { Carrito } from '../model/carrito.model';
import { CarritoItem } from '../model/carrito-item.model';

@Injectable({
  providedIn: 'root'
})
export class GuestCarritoService {
   private base = `${environment.baseUrl}/guest/cart`;

  constructor (private http: HttpClient){ }

  private paramsWithtoken():{ params: HttpParams}{
    const token = getCartToken();
    return { params: new HttpParams().set('token', token)};
  }

  createOrGet(): Observable<Carrito>{
    return this.http.post<Carrito>(this.base, { }, this.paramsWithtoken());
  }

  get(): Observable<Carrito>{
    return this.http.get<Carrito>(this.base, this.paramsWithtoken())
  }

  addItem(libroId: number, cantidad: number): Observable<Carrito>{
    const body = {libroId, cantidad};
    return this.http.post<Carrito>(`${this.base}/items`, body, this.paramsWithtoken())
  }
  updateItem(itemId: number, cantidad: number): Observable<Carrito>{
    const body = {cantidad};
    return this.http.put<Carrito>(`${this.base}/items/${itemId}`, body, this.paramsWithtoken())
  }
  removeItem(CarritoItemId: number): Observable<Carrito>{
    return this.http.delete<Carrito>(`${this.base}/items/${CarritoItemId}`, this.paramsWithtoken())
  }

  clear(){
    return this.http.delete<void>(`${this.base}/clear`, this.paramsWithtoken());
  }

}
