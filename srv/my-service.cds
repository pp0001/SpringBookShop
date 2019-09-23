using { my.bookshop, sap.common } from '../db/data-model';

service CatalogService @(requires: 'authenticated-user'){
  entity Books as projection on bookshop.Books;
  entity Authors @(restrict: [
                     { grant: ['READ','WRITE'], to: 'admin' },
                     { grant: 'READ', to: 'user' },
                   ]) as projection on bookshop.Authors;
  entity Orders @insertonly as projection on bookshop.Orders;
  // TODO @cdsv: should be cds.autoexposed -> then we can remove that:
  entity Countries as projection on common.Countries;
  entity Customers as projection on bookshop.Customers;
  action clearBookStock(bookID:Integer);
  action clearBookStock2(bookID:Integer);
}

service VendorService{
  @odata.draft.enabled
  entity Vendors as projection on bookshop.Vendors;
}