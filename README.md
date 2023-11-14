# orderbook-kotlin

Simple order book that will parse a CSV of orders, match the trades and return top of book. Format is:

The structure of the CSV is `type:userId:ticker:price:quantity:side:orderId`
```
N,123,APPL,200,50,B,789
C,124,790
F
N,125,MSFT,300,30,S,791
C,126,792
```

type can be New order (N), Cancel order (C), Flush (F). side can be Buy (B) or Ask (S)
