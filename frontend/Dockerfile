FROM node:22-alpine

WORKDIR /app

ENV API_PROXY_TARGET=http://backend:8080

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Expose port
EXPOSE 4200

# Start development server
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]
