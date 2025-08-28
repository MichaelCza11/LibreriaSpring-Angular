import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { getCartToken } from '../core/cart-token';

@Injectable({
  providedIn: 'root'
})
export class GuestCheckoutService {
  
  private base = `${environment.baseUrl}/guest/checkout`;

  constructor(http : HttpClient){ }

  checkout(): Observable<Factura>{
    const params = new HttpParams().set('token',getCartToken());
    return this.http.post<Factura>(this.base, {}, {params});
    
  }
}
