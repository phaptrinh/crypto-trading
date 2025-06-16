# Crypto Trading System

A Spring Boot-based cryptocurrency trading platform that aggregates real-time prices from multiple exchanges (Binance, Huobi) and enables secure BTC/ETH trading with USDT.

## 🚀 Quick Start

```bash
# Clone and run
git clone <repository-url>
cd crypto-trading
mvn spring-boot:run

# Access
http://localhost:8080/swagger-ui.html  # API Documentation
http://localhost:8080/h2-console      # Database Console
```

## ✨ Features

- **Real-time Trading**: BUY/SELL BTC/ETH with USDT
- **Price Aggregation**: Best prices from Binance + Huobi (updated every 10s)
- **Multi-Currency Wallets**: USDT, BTC, ETH with real-time balances
- **Idempotency Support**: Prevents duplicate trades
- **Trade History**: Complete audit trail with pagination

## 📡 Core APIs

### Create User & Get Initial Wallets
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "trader1", "email": "trader1@example.com"}'
# Returns user with ID, auto-creates wallets: 50000 USDT, 0 BTC, 0 ETH
```

### Check Prices
```bash
curl http://localhost:8080/api/v1/prices/BTCUSDT
```

### Execute Trade
```bash
curl -X POST http://localhost:8080/api/v1/trades \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "Idempotency-Key: unique-123" \
  -d '{
    "tradingPair": "BTCUSDT",
    "type": "BUY", 
    "quantity": "0.001"
  }'
```

### Check Balances
```bash
curl -H "X-User-Id: 1" http://localhost:8080/api/v1/wallets
```

### View Trade History
```bash
curl -H "X-User-Id: 1" http://localhost:8080/api/v1/trades?page=0&size=10
```

## 🏗️ Architecture

```
External APIs (Binance/Huobi) → Price Aggregation → Best Price Storage
                                       ↓
User Requests → Trade Execution → Wallet Updates → Response
```

**Key Components:**
- **Price Service**: Aggregates best bid/ask from multiple exchanges
- **Trade Service**: Validates balances, executes trades, updates wallets
- **Wallet Service**: Manages multi-currency balances with locking
- **Idempotency**: Prevents duplicate trades with request keys

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.x, Java 17+
- **Database**: H2 (in-memory) with JPA/Hibernate
- **External APIs**: Binance & Huobi price feeds
- **Caching**: Spring Cache for price/wallet data
- **Documentation**: Swagger/OpenAPI 3.0

## ⚙️ Configuration

Key settings in `application.yaml`:
```yaml
app:
  price:
    binance:
      url: https://api.binance.com/api/v3/ticker/bookTicker
    huobi:
      url: https://api.huobi.pro/market/tickers
```

## 📊 Performance

- **Read/Write Ratio**: 85:15 (optimized for price monitoring)
- **Price Updates**: Every 10 seconds
- **Caching**: Price data cached for 5 seconds
- **Database**: Optimized indexes for user queries

## 🔧 Development

```bash
# Run tests
mvn test

# Build
mvn clean package

# H2 Console Access
URL: jdbc:h2:mem:crypto_trading
User: sa, Password: (empty)
```

## 📈 Sample Workflow

1. **Create User** → Auto-creates wallets (50k USDT initial balance)
2. **Monitor Prices** → Real-time aggregated prices from exchanges  
3. **Execute Trades** → BUY/SELL with balance validation
4. **Check Results** → Updated balances and trade history

## 🚀 Production Notes

- Replace H2 with PostgreSQL/MySQL for persistence
- Add authentication/authorization
- Implement rate limiting
- Configure external Redis cache
- Set up monitoring/alerting

---
**Built with Spring Boot • Real-time price aggregation • Secure trading**
